package se.l4.dust.core.internal.template;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.ComputationException;
import com.google.common.collect.MapMaker;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.Stage;

import se.l4.dust.api.Context;
import se.l4.dust.api.NamespaceManager;
import se.l4.dust.api.TemplateException;
import se.l4.dust.api.TemplateManager;
import se.l4.dust.api.annotation.Template;
import se.l4.dust.api.resource.Resource;
import se.l4.dust.api.resource.UrlResource;
import se.l4.dust.api.resource.variant.ResourceVariant;
import se.l4.dust.api.resource.variant.ResourceVariantManager;
import se.l4.dust.api.resource.variant.ResourceVariantManager.ResourceCallback;
import se.l4.dust.api.template.TemplateCache;
import se.l4.dust.api.template.dom.ParsedTemplate;
import se.l4.dust.api.template.spi.internal.XmlTemplateParser;
import se.l4.dust.core.internal.template.dom.TemplateBuilderImpl;

@Singleton
public class TemplateCacheImpl
	implements TemplateCache
{
	private static final Logger logger = LoggerFactory.getLogger(TemplateCacheImpl.class);
	
	private final TemplateManager manager;
	private final InnerCache inner;
	
	private final NamespaceManager namespaces;
	private final ResourceVariantManager variants;
	private final Provider<TemplateBuilderImpl> templateBuilders;

	private final ResourceCallback resourceCallback;
	
	@Inject
	public TemplateCacheImpl(
			TemplateManager manager,
			NamespaceManager namespaces,
			ResourceVariantManager variants,
			Provider<TemplateBuilderImpl> templateBuilders,
			Stage stage)
	{
		this.manager = manager;
		this.namespaces = namespaces;
		this.variants = variants;
		this.templateBuilders = templateBuilders;
		
		inner = stage == Stage.DEVELOPMENT 
			? new DevelopmentCache()
			: new ProductionCache();
		
		resourceCallback = new ResourceVariantManager.ResourceCallback()
		{
			public boolean exists(ResourceVariant variant, String url)
				throws IOException
			{
				try
				{
					InputStream stream = new URL(url).openStream();
					stream.close();
					return true;
				}
				catch(IOException e)
				{
					return false;
				}
			}
		};
		
		logger.info("Template cache is in " + stage + " mode");
	}
	
	/**
	 * Resolve a template based on a class and possibly an annotation. This
	 * is used internally to resolve a URL which used to load the template.
	 * 
	 * @param c
	 * @param annotation
	 * @return
	 * @throws IOException
	 */
	public ParsedTemplate getTemplate(Context context, Class<?> c, Template annotation)
		throws IOException
	{
		if(annotation != null)
		{
			return getTemplate0(context, c, annotation);
		}
		
		Class<?> current = c;
		while(current != Object.class)
		{
			Template t = current.getAnnotation(Template.class);
			if(t != null)
			{
				return getTemplate0(context, current, t);
			}
			
			current = current.getSuperclass();
		}
		
		return getTemplate(context, c, "");
	}

	private ParsedTemplate getTemplate0(Context context, Class<?> c, Template annotation)
		throws IOException
	{
		if(annotation.value() == Object.class)
		{
			return getTemplate(context, c, annotation.name());
		}
		else
		{
			return getTemplate(context, annotation.value(), annotation.name());
		}
	}
	
	private ParsedTemplate getTemplate(Context context, Class<?> c, String name)
		throws IOException
	{
		if(name.equals(""))
		{
			name = c.getSimpleName() + ".xml"; 
		}
		
		URL url = c.getResource(name);
		if(url == null)
		{
			throw new IOException("Could not find template " + name + " besides class " + c);
		}
		
		return getTemplate(context, c, url);
	}
	
	public ParsedTemplate getTemplate(Context context, Class<?> dataContext, URL url)
	{
		try
		{
			return inner.getTemplate(context, dataContext, url);
		}
		catch(ComputationException e)
		{
			if(e.getCause() instanceof RuntimeException)
			{
				throw (RuntimeException) e.getCause();
			}
			else
			{
				throw e;
			}
		}
	}
	
	private ParsedTemplate loadTemplate(Class<?> dataContext, URL url)
	{
		try
		{
			TemplateBuilderImpl builder = templateBuilders.get();
			builder.setContext(url, dataContext);
			
			// TODO: Selection of suitable parser
			XmlTemplateParser parser = new XmlTemplateParser(namespaces, manager);
			
			InputStream in = url.openStream();
			try
			{
				parser.parse(in, url.getPath(), builder);
				
				return builder.getTemplate(); 
			}
			finally
			{
				in.close();
			}
		}
		catch(IOException e)
		{
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	private interface InnerCache
	{
		ParsedTemplate getTemplate(Context context, Class<?> ctx, URL url);
	}
	
	private class ProductionCache
		implements InnerCache
	{
		protected final ConcurrentMap<Key, ParsedTemplate> templates;
		
		public ProductionCache()
		{
			templates = new MapMaker()
				.makeComputingMap(new Function<Key, ParsedTemplate>()
				{
					public ParsedTemplate apply(Key key)
					{
						return loadTemplate(key.context, key.url);
					}
				});
		}
		
		private ParsedTemplate toContextSpecific(Context context, Class<?> ctx, URL url, ParsedTemplate template)
			throws IOException
		{
			Object[] cache = variants.getCacheObject(context);
			Key key = new Key(ctx, url, cache);
			if(templates.containsKey(key))
			{
				return templates.get(key);
			}
			
			// Transform the template
			TemplateVariantImpl impl = new TemplateVariantImpl(variants, context, template, url.toExternalForm());
			impl.transform();
			
			// Check if this transformation has already been cached
			URL transformedUrl = new URL(impl.getTransformedUrl());
			Key secondKey = new Key(ctx, transformedUrl, new Object[0]);
			
			ParsedTemplate cacheTemplate;
			if(! templates.containsKey(secondKey))
			{
				cacheTemplate = url.equals(transformedUrl)
					? template
					: impl.getTransformedTemplate();
				
				templates.put(secondKey, cacheTemplate);
			}
			else
			{
				cacheTemplate = templates.get(secondKey);
			}
			
			templates.put(key, cacheTemplate);
			
			return cacheTemplate;
		}
		
		public ParsedTemplate getTemplate(Context context, Class<?> ctx, URL url)
		{
			String raw = url.toExternalForm();
			try
			{
				raw = variants.resolve(context, resourceCallback, raw);
				ParsedTemplate template = templates.get(new Key(ctx, new URL(raw)));
				
				return toContextSpecific(context, ctx, url, template);
			}
			catch(IOException e)
			{
				throw new TemplateException("Unable to load " + raw + "; " + e.getMessage(), e);
			}
			catch(ComputationException e)
			{
				throw new TemplateException("Unable to load " + raw + "; " + e.getCause().getMessage(), e.getCause());
			}
		}
	}
	
	private class DevelopmentCache
		implements InnerCache
	{
		protected final ConcurrentMap<Key, DevParsedTemplate> templates;
		
		public DevelopmentCache()
		{
			templates = new MapMaker()
				.makeComputingMap(new Function<Key, DevParsedTemplate>()
				{
					public DevParsedTemplate apply(Key key)
					{
						return new DevParsedTemplate(loadTemplate(key.context, key.url), getResource(key.url));
					}
				});
		}
		
		public ParsedTemplate getTemplate(Context context, Class<?> ctx, URL url)
		{
			String raw = url.toExternalForm();
			try
			{
				raw = variants.resolve(context, resourceCallback, raw);
				url = new URL(raw);
			
				DevParsedTemplate template = templates.get(new Key(ctx, url));
				Resource resource = template.resource;
				Resource newResource = getResource(url);
				if(resource.getLastModified() < newResource.getLastModified())
				{
					// Modified, reload the template
					template = new DevParsedTemplate(loadTemplate(ctx, url), newResource);
					templates.put(new Key(ctx, url), template);
				}
				
				return template;
			}
			catch(IOException e)
			{
				throw new TemplateException("Unable to load " + raw + "; " + e.getMessage(), e);
			}
			catch(ComputationException e)
			{
				throw new TemplateException("Unable to load " + raw + "; " + e.getCause().getMessage(), e.getCause());
			}
		}
		
		private Resource getResource(URL url)
		{
			try
			{
				return new UrlResource(url);
			}
			catch(IOException e)
			{
				throw new TemplateException("Could not create reference to resource");
			}
		}
	}
	
	private static class DevParsedTemplate
		extends ParsedTemplate
	{
		private final Resource resource;

		public DevParsedTemplate(ParsedTemplate tpl, Resource resource)
		{
			super(tpl.getDocType(), tpl.getRoot());
			
			this.resource = resource;
		}
	}
	
	private static class Key
	{
		private final Class<?> context;
		private final URL url;
		private final Object[] extra;

		public Key(Class<?> context, URL url)
		{
			this(context, url, null);
		}
		
		public Key(Class<?> context, URL url, Object[] extra)
		{
			this.context = context;
			this.url = url;
			this.extra = extra;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((context == null)
				? 0
				: context.hashCode());
			result = prime * result + Arrays.hashCode(extra);
			result = prime * result + ((url == null)
				? 0
				: url.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if(this == obj)
				return true;
			if(obj == null)
				return false;
			if(getClass() != obj.getClass())
				return false;
			Key other = (Key) obj;
			if(context == null)
			{
				if(other.context != null)
					return false;
			}
			else if(!context.equals(other.context))
				return false;
			if(!Arrays.equals(extra, other.extra))
				return false;
			if(url == null)
			{
				if(other.url != null)
					return false;
			}
			else if(!url.equals(other.url))
				return false;
			return true;
		}
	}
}
