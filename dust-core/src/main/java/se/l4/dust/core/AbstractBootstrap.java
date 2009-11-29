package se.l4.dust.core;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.spi.Registry;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import se.l4.dust.core.internal.PageDiscovery;

import com.google.inject.Injector;

public abstract class AbstractBootstrap
	implements ServletContextListener
{
	public void contextDestroyed(ServletContextEvent sce)
	{
	}

	public void contextInitialized(ServletContextEvent sce)
	{
		ServletContext ctx = sce.getServletContext();
		Injector i = getInjector(ctx); 
		
		// Setup Resteasy
		ResteasyProviderFactory factory = i.getInstance(ResteasyProviderFactory.class);
		Dispatcher dispatcher = i.getInstance(Dispatcher.class);
		Registry registry = i.getInstance(Registry.class);
		
		// Register the services in the context
		ctx.setAttribute(Injector.class.getName(), i);
		ctx.setAttribute(ResteasyProviderFactory.class.getName(), factory);
		ctx.setAttribute(Dispatcher.class.getName(), dispatcher);
		ctx.setAttribute(Registry.class.getName(), registry);
		
		try
		{
			i.getInstance(PageDiscovery.class).discover(ctx);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	protected abstract Injector getInjector(ServletContext sce);
}
