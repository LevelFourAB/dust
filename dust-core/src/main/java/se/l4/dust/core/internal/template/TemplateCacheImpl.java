package se.l4.dust.core.internal.template;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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

import se.l4.dust.api.NamespaceManager;
import se.l4.dust.api.TemplateException;
import se.l4.dust.api.TemplateManager;
import se.l4.dust.api.annotation.Template;
import se.l4.dust.api.resource.Resource;
import se.l4.dust.api.resource.UrlResource;
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
	private final Provider<TemplateBuilderImpl> templateBuilders;
	
	@Inject
	public TemplateCacheImpl(
			TemplateManager manager,
			NamespaceManager namespaces,
			Provider<TemplateBuilderImpl> templateBuilders,
			Stage stage)
	{
		this.manager = manager;
		this.namespaces = namespaces;
		this.templateBuilders = templateBuilders;
		
		inner = stage == Stage.DEVELOPMENT 
			? new DevelopmentCache()
			: new ProductionCache();
			
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
	public ParsedTemplate getTemplate(Class<?> c, Template annotation)
		throws IOException
	{
		if(annotation != null)
		{
			return getTemplate0(c, annotation);
		}
		
		Class<?> current = c;
		while(current != Object.class)
		{
			Template t = current.getAnnotation(Template.class);
			if(t != null)
			{
				return getTemplate0(current, t);
			}
			
			current = current.getSuperclass();
		}
		
		return getTemplate(c, "");
	}

	private ParsedTemplate getTemplate0(Class<?> c, Template annotation)
		throws IOException
	{
		if(annotation.value() == Object.class)
		{
			return getTemplate(c, annotation.name());
		}
		else
		{
			return getTemplate(annotation.value(), annotation.name());
		}
	}
	
	private ParsedTemplate getTemplate(Class<?> c, String name)
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
		
		return getTemplate(c, url);
	}
	
	public ParsedTemplate getTemplate(Class<?> context, URL url)
	{
		try
		{
			return inner.getTemplate(context, url);
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
	
	private ParsedTemplate loadTemplate(Class<?> context, URL url)
	{
		try
		{
			TemplateBuilderImpl builder = templateBuilders.get();
			builder.setContext(context);
			
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
		ParsedTemplate getTemplate(Class<?> ctx, URL url);
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
		
		public ParsedTemplate getTemplate(Class<?> ctx, URL url)
		{
			return templates.get(new Key(ctx, url));
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
		
		public ParsedTemplate getTemplate(Class<?> ctx, URL url)
		{
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
	
	private class Key
	{
		private final Class<?> context;
		private final URL url;

		public Key(Class<?> context, URL url)
		{
			this.context = context;
			this.url = url;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((context == null)
				? 0
				: context.hashCode());
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
			if(!getOuterType().equals(other.getOuterType()))
				return false;
			if(context == null)
			{
				if(other.context != null)
					return false;
			}
			else if(!context.equals(other.context))
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

		private TemplateCacheImpl getOuterType()
		{
			return TemplateCacheImpl.this;
		}
	}
}
