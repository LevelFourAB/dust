package se.l4.dust.core.internal.resource;

import se.l4.crayon.CrayonModule;
import se.l4.crayon.annotation.Contribution;
import se.l4.dust.api.resource.variant.ResourceVariantManager;

/**
 * Module for binding internal resource classes.
 * 
 * @author Andreas Holstenson
 *
 */
public class ResourceModule
	extends CrayonModule
{

	@Override
	protected void configure()
	{
		bind(ResourceVariantManager.class).to(ResourceVariantManagerImpl.class);
	}

	@Contribution(name="locale-variant")
	public void bindLocaleVariantSource(ResourceVariantManager vm, LocaleVariantSource source)
	{
		vm.addSource(source);
	}
}
