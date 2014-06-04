package se.l4.dust.core.internal.template;

import java.io.IOException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.l4.dust.api.Context;
import se.l4.dust.api.Namespaces;
import se.l4.dust.api.resource.Resource;
import se.l4.dust.api.resource.ResourceLocation;
import se.l4.dust.api.resource.UrlLocation;
import se.l4.dust.api.resource.UrlResource;
import se.l4.dust.api.resource.variant.ResourceVariantManager;
import se.l4.dust.api.template.Template;
import se.l4.dust.api.template.TemplateCache;
import se.l4.dust.api.template.TemplateException;
import se.l4.dust.api.template.Templates;
import se.l4.dust.api.template.dom.ParsedTemplate;
import se.l4.dust.api.template.spi.XmlTemplateParser;
import se.l4.dust.core.internal.template.dom.TemplateBuilderImpl;

import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
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
	
	private final InnerCache inner;
	
	private final Provider<TemplateBuilderImpl> templateBuilders;

	private XmlTemplateParser parser;
	
	@Inject
	public TemplateCacheImpl(
			Templates manager,
			Namespaces namespaces,
			ResourceVariantManager variants,
			XmlTemplateParser parser,
			Provider<TemplateBuilderImpl> templateBuilders,
			Stage stage)
	{
		this.parser = parser;
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
	@Override
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
	
	@Override
	public ParsedTemplate getTemplate(Context context, Class<?> dataContext, URL url)
		throws IOException
	{
		return getTemplate(context, dataContext, new UrlResource(new UrlLocation(url), url));
	}
	
	public ParsedTemplate getTemplate(Context context, Class<?> dataContext, Resource resource)
	{
		try
		{
			return inner.getTemplate(context, dataContext, resource);
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
	
	private ParsedTemplate loadTemplate(Class<?> dataContext, Resource resource)
	{
		try
		{
			TemplateBuilderImpl builder = templateBuilders.get();
			builder.setContext(resource.getLocation(), dataContext);
			
			parser.parse(resource, builder);
			
			return builder.getTemplate(); 
		}
		catch(IOException e)
		{
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	private interface InnerCache
	{
		ParsedTemplate getTemplate(Context context, Class<?> ctx, Resource resource);
		
		ContextKey getTemplateUrl(Class<?> ctx);
	}
	
	private class ProductionCache
		implements InnerCache
	{
		private final Cache<Key, ParsedTemplate> templates;
		private final LoadingCache<Class<?>, ContextKey> urlCache;
		
		public ProductionCache()
		{
			templates = CacheBuilder.newBuilder().build();
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
		
		@Override
		public ParsedTemplate getTemplate(Context context, Class<?> ctx, Resource resource)
		{
			Key key = new Key(ctx, resource.getLocation());
			ParsedTemplate template = templates.getIfPresent(key);
			if(template != null) return template;
			
			template = loadTemplate(ctx, resource);
			templates.put(key, template);
			
			return template;
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
		private final Cache<Key, DevParsedTemplate> templates;
		
		public DevelopmentCache()
		{
			templates = CacheBuilder.newBuilder().build();
		}
		
		@Override
		public ParsedTemplate getTemplate(Context context, Class<?> ctx, Resource resource)
		{
			Key key = new Key(ctx, resource.getLocation());
			DevParsedTemplate template = templates.getIfPresent(key);
			if(template == null || template.resource.getLastModified() < resource.getLastModified())
			{
				// Modified, reload the template
				template = new DevParsedTemplate(loadTemplate(ctx, resource), resource);
				templates.put(key, template);
			}
			
			return template;
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
			super(tpl.getLocation(), tpl.getName(), tpl.getDocType(), tpl.getRoot(), tpl.getRawId());
			
			this.resource = resource;
		}
	}
	
	public static class Key
	{
		private final Class<?> context;
		private final ResourceLocation location;

		public Key(Class<?> context, ResourceLocation location)
		{
			this.context = context;
			this.location = location;
		}
		
		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((context == null) ? 0 : context.hashCode());
			result = prime * result
					+ ((location == null) ? 0 : location.hashCode());
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
			if(location == null)
			{
				if(other.location != null)
					return false;
			}
			else if(!location.equals(other.location))
				return false;
			return true;
		}

		@Override
		public String toString()
		{
			return "Key{context=" + context + ", location=" + location + "}";
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
