package se.l4.dust.jaxrs.internal;

import java.io.IOException;

import javax.ws.rs.Path;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.l4.dust.api.Context;
import se.l4.dust.api.NamespaceManager;
import se.l4.dust.api.TemplateException;
import se.l4.dust.api.annotation.Component;
import se.l4.dust.api.annotation.Template;
import se.l4.dust.api.discovery.ClassDiscovery;
import se.l4.dust.api.discovery.DiscoveryFactory;
import se.l4.dust.api.resource.variant.ResourceVariantManager;
import se.l4.dust.api.template.TemplateCache;
import se.l4.dust.jaxrs.PageManager;
import se.l4.dust.jaxrs.spi.Configuration;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Stage;

/**
 * Module that performs a single contribution scanning the classpath for
 * classes that are either pages or components. It then checks if they are
 * within a registered package and if so registers them automatically.
 * 
 * @author Andreas Holstenson
 *
 */
public class PageDiscovery
{
	private static final Logger logger = LoggerFactory.getLogger(PageDiscovery.class);
	private final NamespaceManager manager;
	private final PageManager pages;
	private final TemplateCache templateCache;
	private final ResourceVariantManager variants;
	private final Stage stage;
	private final DiscoveryFactory discovery;
	private final Configuration configuration;
	private final Injector injector;
	
	@Inject
	public PageDiscovery(
			Stage stage,
			Injector injector,
			NamespaceManager manager,
			PageManager pages,
			Configuration configuration,
			TemplateCache templateCache,
			ResourceVariantManager variants,
			DiscoveryFactory discovery)
	{
		this.injector = injector;
		this.manager = manager;
		this.pages = pages;
		this.configuration = configuration;
		this.templateCache = templateCache;
		this.variants = variants;
		this.stage = stage;
		this.discovery = discovery;
	}

	public void discover()
		throws Exception
	{
		discover(false, false);
	}
	
	public void reindexAndDiscover()
		throws Exception
	{
		discover(true, true);
	}
	
	private void discover(boolean silent, boolean index)
		throws Exception
	{
		if(! silent)
		{
			logger.info("Attempting to discover classes within registered namespaces");
		}

		for(NamespaceManager.Namespace ns : manager)
		{
			handleNamespace(ns, silent, index);
		}
	}
	
	private void handleNamespace(NamespaceManager.Namespace ns, boolean silent, boolean index)
		throws IOException
	{
		String pkg = ns.getPackage();
		// Skip namespaces without packages
		if(pkg == null) return;
		
		ClassDiscovery cd = discovery.get(ns.getPackage());
		if(index)
		{
			cd.index();
		}
		
		int pages = handlePages(cd);
		int providers = handleProviders(cd);
		
		if(! silent)
		{
			String message = ns.getUri() + ": Found "
				+ pages + " pages and " 
				+ providers + " providers";
			
			logger.info(message);
		}
		
		if(stage == Stage.PRODUCTION)
		{
			int templates = handleTemplates(cd);

			if(! silent)
			{
				logger.info(ns.getUri() + ": Loaded " + templates + " templates");
			}
		}
	}
	
	/**
	 * Handle all pages found (everything annotated with {@link Path}).
	 * 
	 * @param manager
	 * @param pages
	 * @param s
	 * @return
	 * @throws Exception
	 */
	private int handlePages(ClassDiscovery cd)
	{
		int count = 0;
		for(Class<?> type : cd.getAnnotatedWith(Path.class))
		{
			pages.add(type);
			count++;
		}
		
		return count;
	}
	
	/**
	 * Handle all pages found (everything annotated with {@link Path}).
	 * 
	 * @param manager
	 * @param pages
	 * @param s
	 * @return
	 * @throws Exception
	 */
	private int handleProviders(ClassDiscovery cd)
	{
		int count = 0;
		for(Class<?> type : cd.getAnnotatedWith(Provider.class))
		{
			Object instance = null;
			if(MessageBodyReader.class.isAssignableFrom(type))
			{
				if(instance == null) instance = injector.getInstance(type);
				configuration.addMessageBodyReader((MessageBodyReader<?>) instance);
				count++;
			}
			
			if(MessageBodyWriter.class.isAssignableFrom(type))
			{
				if(instance == null) instance = injector.getInstance(type);
				configuration.addMessageBodyWriter((MessageBodyWriter<?>) instance);
				count++;
			}
			
			if(ExceptionMapper.class.isAssignableFrom(type))
			{
				if(instance == null) instance = injector.getInstance(type);
				configuration.addExceptionMapper((ExceptionMapper<?>) instance);
				count++;
			}
		}
		
		return count;
	}
	
	/**
	 * Handle all pages found (everything annotated with {@link Path}).
	 * 
	 * @param manager
	 * @param pages
	 * @param s
	 * @return
	 * @throws Exception
	 */
	private int handleTemplates(ClassDiscovery cd)
		throws IOException
	{
		int count = 0;
		
		/**
		 * Go through all classes annotated with Template.
		 */
		for(Class<?> type : cd.getAnnotatedWith(Template.class))
		{
			for(Context ctx : variants.getInitialContexts())
			{
				templateCache.getTemplate(ctx, type, type.getAnnotation(Template.class));
			}
			
			count++;
		}
		
		/*
		 * Go through components and cache their templates. Ignore their
		 * errors though as certain components do not have templates.
		 */
		for(Class<?> type : cd.getAnnotatedWith(Component.class))
		{
			for(Context ctx : variants.getInitialContexts())
			{
				try
				{
					templateCache.getTemplate(ctx, type, type.getAnnotation(Template.class));
				}
				catch(TemplateException e)
				{
					// Ignore this exception
				}
			}
			
			count++;
		}
		
		return count;
	}
}
