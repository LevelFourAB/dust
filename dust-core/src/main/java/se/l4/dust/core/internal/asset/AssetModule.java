package se.l4.dust.core.internal.asset;

import com.google.inject.name.Named;

import se.l4.crayon.Contribution;
import se.l4.crayon.Contributions;
import se.l4.crayon.CrayonModule;
import se.l4.crayon.Order;
import se.l4.dust.api.asset.AssetContribution;
import se.l4.dust.api.asset.Assets;
import se.l4.dust.api.resource.Resources;
import se.l4.dust.core.internal.InternalContributions;

public class AssetModule
	extends CrayonModule
{
	@Override
	public void configure()
	{
		bind(Assets.class).to(AssetsImpl.class);
		bindContributions(AssetContribution.class);
	}

	@Contribution
	@Named("internal-asset-project")
	public void contributeDefaultProtectedExtensions(Assets manager)
	{
		manager.addProtectedExtension("xml");
		manager.addProtectedExtension("class");
	}

	@Contribution
	@Order({ "after:internal-resource-locators", "before:resource-locators" })
	public void contributeAssetResourceLocator(Resources resources, BuiltAssetLocator locator)
	{
		resources.addLocator(locator);
	}

	@Contribution
	@Named("dust-assets")
	@Order({ "after:dust-namespaces", "after:resource-locators" })
	public void contributeAssets(@AssetContribution Contributions contributions)
	{
		InternalContributions.add(contributions);

		contributions.run();
	}
}
