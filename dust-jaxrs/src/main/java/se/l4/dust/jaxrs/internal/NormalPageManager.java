package se.l4.dust.jaxrs.internal;

import java.io.IOException;
import java.lang.reflect.Method;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.Stage;

import se.l4.dust.api.Context;
import se.l4.dust.api.annotation.Template;
import se.l4.dust.api.resource.variant.ResourceVariantManager;
import se.l4.dust.api.template.TemplateCache;
import se.l4.dust.jaxrs.PageManager;
import se.l4.dust.jaxrs.PageProvider;
import se.l4.dust.jaxrs.spi.Configuration;


@Singleton
public class NormalPageManager
	implements PageManager
{
	private final Configuration config;
	private final TemplateCache templates;
	private final ResourceVariantManager variants;
	private final Injector injector;
	private final Stage stage;
	
	@Inject
	public NormalPageManager(Configuration config,
			TemplateCache templates,
			ResourceVariantManager variants,
			Injector injector,
			Stage stage)
	{
		this.config = config;
		this.templates = templates;
		this.variants = variants;
		this.injector = injector;
		this.stage = stage;
	}
	
	public PageManager add(final Class<?> page)
	{
		config.addPage(new PageProvider()
		{
			public Object get()
			{
				return injector.getInstance(page);
			}
			
			public Class<?> getType()
			{
				return page;
			}
			
			public void release(Object o)
			{
			}
		});
		
		if(stage == Stage.PRODUCTION) 
		{
			try
			{
				cache(page);
			}
			catch(IOException e)
			{
				throw new Error("Unable to cache template for " + page, e);
			}
		}
		
		return this;
	}

	private void cache(Class<?> page)
		throws IOException
	{
		cacheTemplate(page);
		
		for(Method method : page.getMethods())
		{
			cacheTemplate(method.getReturnType());
		}
	}
	
	private void cacheTemplate(Class<?> tpl)
		throws IOException
	{
		if(tpl.isAnnotationPresent(Template.class))
		{
			for(Context ctx : variants.getInitialContexts())
			{
				templates.getTemplate(ctx, tpl, tpl.getAnnotation(Template.class));
			}
		}
	}
}
