package se.l4.dust.core;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import se.l4.crayon.annotation.Contribution;
import se.l4.crayon.annotation.Shutdown;
import se.l4.crayon.osgi.ExportManager;
import se.l4.crayon.osgi.OSGiConfigurator;
import se.l4.dust.api.PageProviderManager;


/**
 * The activator used to configure the webservices and setup so pages can
 * be exposed.
 * 
 * @author andreas
 *
 */
public class WebBundleActivator
	implements BundleActivator
{
	private ServiceRegistration pageManager;
	private OSGiConfigurator configurator;
	
	@Contribution
	public void contributeExports(ExportManager manager)
	{
		manager.export(PageProviderManager.class);
	}
	
	@Shutdown
	public void closeExports(ExportManager manager)
	{
		manager.remove(pageManager);
	}

	public void start(BundleContext ctx)
		throws Exception
	{
		configurator = new OSGiConfigurator(ctx)
			.add(WebModule.class)
			.addInstance(this);
		
		configurator.configure();
	}

	public void stop(BundleContext ctx)
		throws Exception
	{
		configurator.shutdown();
	}
}
