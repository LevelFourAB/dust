package se.l4.dust.servlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import se.l4.crayon.Configurator;
import se.l4.dust.Dust;

import com.google.inject.Injector;
import com.google.inject.Stage;

/**
 * Bootstrap that delegates to a {@link Configurator}.
 *
 * @author Andreas Holstenson
 *
 */
public abstract class AppBootstrap
	extends AbstractBootstrap
{
	private Configurator configurator;

	@Override
	public void contextDestroyed(ServletContextEvent sce)
	{
		configurator.shutdown();
	}

	@Override
	protected Injector getInjector(ServletContext sce)
	{
		String systemProperty = System.getProperty(Dust.DUST_PRODUCTION);
		String productionStr = systemProperty == null
			? sce.getInitParameter(Dust.DUST_PRODUCTION)
			: systemProperty;

		boolean production = ! "false".equalsIgnoreCase(productionStr);

		// New context, let's initialize the system
		configurator = new Configurator(production ? Stage.PRODUCTION : Stage.DEVELOPMENT);

		initialize(configurator);

		configurator.configure();

		return configurator.getInjector();
	}

	protected abstract void initialize(Configurator configurator);
}
