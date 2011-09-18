package se.l4.dust.jrebel;

import org.zeroturnaround.javarebel.ClassResourceSource;
import org.zeroturnaround.javarebel.Integration;
import org.zeroturnaround.javarebel.IntegrationFactory;
import org.zeroturnaround.javarebel.Plugin;

/**
 * Plugin for Dust. Integrates with Crayon modules and makes sure that changes 
 * to contributions are seen in runtime.
 *  
 * @author Andreas Holstenson
 *
 */
public class DustPlugin
	implements Plugin
{
	@Override
	public void preinit()
	{
		Integration integration = IntegrationFactory.getInstance();
		integration.addIntegrationProcessor("se.l4.crayon.CrayonBinder$RealBinder", new CrayonBinderCBP());
	}

	@Override
	public boolean checkDependencies(ClassLoader cl, ClassResourceSource crs)
	{
		return crs.getClassResource("se.l4.dust.api.Context") != null;
	}

	@Override
	public String getId()
	{
		return "dust_plugin";
	}

	@Override
	public String getName()
	{
		return "Dust Plugin";
	}

	@Override
	public String getDescription()
	{
		return "Enables automatic finding of new pages and assets";
	}

	@Override
	public String getAuthor()
	{
		return null;
	}

	@Override
	public String getWebsite()
	{
		return null;
	}

}
