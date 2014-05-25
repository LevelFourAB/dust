package se.l4.dust.core.internal.asset;

import se.l4.crayon.Contributions;
import se.l4.crayon.CrayonModule;
import se.l4.crayon.annotation.Contribution;
import se.l4.crayon.annotation.Order;
import se.l4.dust.api.asset.AssetContribution;
import se.l4.dust.api.asset.Assets;
import se.l4.dust.api.template.Templates;
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
	
	@Contribution(name="internal-asset-protect")
	public void contributeDefaultProtectedExtensions(Assets manager)
	{
		manager.addProtectedExtension("xml");
		manager.addProtectedExtension("class");
	}
	
	@Contribution(name="internal-asset-sources")
	public void contributeClasspathSource(Assets manager)
	{
		manager.addSource(ClasspathAssetSource.class);
	}
	
	@Contribution(name="dust-assets")
	@Order({ "after:dust-namespaces", "after:internal-asset-sources" })
	public void contributeAssets(@AssetContribution Contributions contributions)
	{
		InternalContributions.add(contributions);
		
		contributions.run();
	}
}
