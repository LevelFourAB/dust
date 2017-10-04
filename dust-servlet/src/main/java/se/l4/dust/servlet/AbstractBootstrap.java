package se.l4.dust.servlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import se.l4.crayon.Contributions;
import se.l4.dust.api.discovery.NamespaceDiscovery;

import com.google.inject.Injector;
import com.google.inject.Key;

/**
 * Abstract bootstrap for setting up a default installation.
 *
 * @author Andreas Holstenson
 *
 */
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
		ctx.setAttribute(Injector.class.getName(), i);

		// Pass the context along
		WebScopes.setContext(ctx);

		// Setup the rest of the context
		Contributions contextContributions = i.getInstance(
			Key.get(Contributions.class, ContextContribution.class)
		);
		contextContributions.run();

		try
		{
			i.getInstance(NamespaceDiscovery.class).performDiscovery();;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	protected abstract Injector getInjector(ServletContext sce);
}
