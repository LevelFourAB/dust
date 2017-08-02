package se.l4.dust.core.internal.resource;

import com.google.inject.name.Named;

import se.l4.crayon.Contribution;
import se.l4.crayon.CrayonModule;
import se.l4.crayon.Order;
import se.l4.dust.api.resource.Resources;
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
		bind(Resources.class).to(ResourcesImpl.class);
		bind(ResourceVariantManager.class).to(ResourceVariantManagerImpl.class);
	}

	@Contribution
	@Named("internal-resource-locators")
	public void contributeClasspathSource(Resources resources, ClasspathResourceLocator classpath)
	{
		resources.addLocator(classpath);
	}

	@Contribution
	@Named("locale-variant")
	public void bindLocaleVariantSource(ResourceVariantManager vm, LocaleVariantSource source)
	{
		vm.addSource(source);
	}

	@Contribution
	@Named("resource-locators")
	@Order("after:internal-resource-locators")
	public void resourceLocators()
	{
		// Currently empty - used for ordering
	}
}
