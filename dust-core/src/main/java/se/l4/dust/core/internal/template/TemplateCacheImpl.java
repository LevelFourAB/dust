package se.l4.dust.core.internal.template;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.l4.dust.api.Context;
import se.l4.dust.api.Namespaces;
import se.l4.dust.api.resource.Resource;
import se.l4.dust.api.resource.UrlResource;
import se.l4.dust.api.resource.variant.ResourceVariant;
import se.l4.dust.api.resource.variant.ResourceVariantManager;
import se.l4.dust.api.resource.variant.ResourceVariantManager.ResourceCallback;
import se.l4.dust.api.template.Template;
import se.l4.dust.api.template.TemplateCache;
import se.l4.dust.api.template.TemplateException;
import se.l4.dust.api.template.Templates;
import se.l4.dust.api.template.dom.ParsedTemplate;
import se.l4.dust.api.template.spi.internal.XmlTemplateParser;
import se.l4.dust.core.internal.template.dom.TemplateBuilderImpl;

import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ComputationException;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.Stage;

/**
 * Implementation of {@link TemplateCache}.
 * 
 * @author Andreas Holstenson
 *
 */
@Singleton
public class TemplateCacheImpl
	implements TemplateCache
{
	private static final Logger logger = LoggerFactory.getLogger(TemplateCacheImpl.class);
	
	private final Templates manager;
	private final InnerCache inner;
	
	private final Namespaces namespaces;
	private final ResourceVariantManager variants;
	private final Provider<TemplateBuilderImpl> templateBuilders;

	private final ResourceCallback resourceCallback;
	
	@Inject
	public TemplateCacheImpl(
			Templates manager,
			Namespaces namespaces,
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
		try
		{
			ContextKey tpl = inner.getTemplateUrl(c);
			return getTemplate(context, tpl.ctx, tpl.url);
		}
		catch(ComputationException e)
		{
			Throwables.propagateIfPossible(e.getCause());
			throw e;
		}
	}
	
	private ContextKey findTemplateUrl(Class<?> c)
	{
		Class<?> current = c;
		while(current != Object.class)
		{
			Template t = current.getAnnotation(Template.class);
			if(t != null)
			{
				return findTemplateUrl(current, t);
			}
			
			current = current.getSuperclass();
		}
		
		return findTemplateUrl(c, "");
	}

	private ContextKey findTemplateUrl(Class<?> c, Template annotation)
	{
		if(annotation.value() == Object.class)
		{
			return findTemplateUrl(c, annotation.name());
		}
		else
		{
			return findTemplateUrl(annotation.value(), annotation.name());
		}
	}
	
	private ContextKey findTemplateUrl(Class<?> c, String name)
	{
		if(name.equals(""))
		{
			name = c.getSimpleName() + ".xml"; 
		}
		
		URL url = c.getResource(name);
		if(url == null)
		{
			throw new TemplateException("Could not find template " + name + " besides class " + c);
		}
		
		return new ContextKey(c, url);
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
		
		ContextKey getTemplateUrl(Class<?> ctx);
	}
	
	private class ProductionCache
		implements InnerCache
	{
		private final LoadingCache<Key, ParsedTemplate> templates;
		private final LoadingCache<Class<?>, ContextKey> urlCache;
		
		public ProductionCache()
		{
			templates = CacheBuilder.newBuilder()
				.build(new CacheLoader<Key, ParsedTemplate>()
				{
					@Override
					public ParsedTemplate load(Key key)
					{
						return loadTemplate(key.context, key.url);
					}
				});
			

			urlCache = CacheBuilder.newBuilder()
				.build(new CacheLoader<Class<?>, ContextKey>()
				{
					@Override
					public ContextKey load(Class<?> input)
					{
						return findTemplateUrl(input);
					}
				});
		}
		
		public ParsedTemplate getTemplate(Context context, Class<?> ctx, URL url)
		{
			try
			{
				Key key = new Key(ctx, url);
				return templates.get(key);
			}
			catch(ExecutionException e)
			{
				throw new TemplateException("Unable to load " + url + "; " + e.getCause().getMessage(), e.getCause());
			}
		}
		
		@Override
		public ContextKey getTemplateUrl(Class<?> ctx)
		{
			try
			{
				return urlCache.get(ctx);
			}
			catch(Exception e)
			{
				Throwables.propagateIfInstanceOf(e, TemplateException.class);
				Throwables.propagateIfInstanceOf(e.getCause(), TemplateException.class);
				throw new TemplateException("Unable to get url for " + ctx + "; " + e.getCause().getMessage(), e.getCause());
			}
		}
	}
	
	private class DevelopmentCache
		implements InnerCache
	{
		protected final LoadingCache<Key, DevParsedTemplate> templates;
		
		public DevelopmentCache()
		{
			templates = CacheBuilder.newBuilder()
				.build(new CacheLoader<Key, DevParsedTemplate>()
				{
					@Override
					public DevParsedTemplate load(Key key)
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
				ResourceVariantManager.Result result = variants.resolve(context, resourceCallback, raw);
				url = new URL(result.getUrl());
			
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
			catch(ExecutionException e)
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
		
		@Override
		public ContextKey getTemplateUrl(Class<?> ctx)
		{
			return findTemplateUrl(ctx);
		}
	}
	
	private static class DevParsedTemplate
		extends ParsedTemplate
	{
		private final Resource resource;

		public DevParsedTemplate(ParsedTemplate tpl, Resource resource)
		{
			super(tpl.getUrl(), tpl.getName(), tpl.getDocType(), tpl.getRoot(), tpl.getRawId());
			
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
		
		@Override
		public String toString()
		{
			return "Key{context=" + context + ", url=" + url + "}";
		}
	}
	
	/**
	 * Key used to lookup URLs for classes.
	 * 
	 * @author Andreas Holstenson
	 *
	 */
	private static class ContextKey
	{
		private final Class<?> ctx;
		private final URL url;

		public ContextKey(Class<?> ctx, URL url)
		{
			this.ctx = ctx;
			this.url = url;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((ctx == null)
				? 0
				: ctx.hashCode());
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
			ContextKey other = (ContextKey) obj;
			if(ctx == null)
			{
				if(other.ctx != null)
					return false;
			}
			else if(!ctx.equals(other.ctx))
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
