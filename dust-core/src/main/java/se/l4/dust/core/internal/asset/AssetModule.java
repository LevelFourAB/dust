package se.l4.dust.core.internal.asset;

import se.l4.crayon.Contributions;
import se.l4.crayon.CrayonModule;
import se.l4.crayon.annotation.Contribution;
import se.l4.crayon.annotation.Order;
import se.l4.dust.api.TemplateManager;
import se.l4.dust.api.annotation.Assets;
import se.l4.dust.api.asset.AssetManager;

public class AssetModule
	extends CrayonModule
{
	@Override
	public void configure()
	{
		bind(AssetManager.class).to(AssetManagerImpl.class);
		bindContributions(Assets.class);
	}
	
	@Contribution(name="internal-asset-protect")
	public void contributeDefaultProtectedExtensions(AssetManager manager)
	{
		manager.addProtectedExtension("xml");
		manager.addProtectedExtension("class");
	}
	
	@Contribution(name="internal-asset-property-source")
	public void contributeAssetPropertySource(TemplateManager manager)
	{
		manager.addPropertySource("asset", AssetPropertySource.class);
		manager.addPropertySource("a", AssetPropertySource.class);
	}
	
	@Contribution(name="internal-asset-sources")
	public void contributeClasspathSource(AssetManager manager)
	{
		manager.addSource(ClasspathAssetSource.class);
	}
	
	@Contribution(name="dust-assets")
	@Order({ "after:dust-namespaces", "after:internal-asset-sources" })
	public void contributeAssets(@Assets Contributions contributions)
	{
		contributions.run();
	}
}
