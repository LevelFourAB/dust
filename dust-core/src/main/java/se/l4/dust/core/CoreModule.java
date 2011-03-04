package se.l4.dust.core;

import se.l4.crayon.CrayonModule;
import se.l4.dust.api.NamespaceManager;
import se.l4.dust.core.internal.NamespaceManagerImpl;
import se.l4.dust.core.internal.asset.AssetModule;
import se.l4.dust.core.internal.conversion.ConversionModule;
import se.l4.dust.core.internal.template.TemplateModule;

/**
 * Module containing core functionality.
 * 
 * @author Andreas Holstenson
 *
 */
public class CoreModule
	extends CrayonModule
{

	@Override
	protected void configure()
	{
		install(new AssetModule());
		install(new TemplateModule());
		install(new ConversionModule());
		
		bind(NamespaceManager.class).to(NamespaceManagerImpl.class);
	}

}
