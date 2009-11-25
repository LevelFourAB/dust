package se.l4.dust.core.internal.asset;

import org.jboss.resteasy.spi.ResteasyProviderFactory;

import com.google.inject.Binder;

import se.l4.crayon.annotation.Contribution;
import se.l4.crayon.annotation.Description;
import se.l4.dust.api.PageManager;
import se.l4.dust.api.TemplateManager;
import se.l4.dust.api.asset.AssetManager;
import se.l4.dust.core.asset.AssetProvider;

public class AssetModule
{
	@Description(name="asset")
	public void configure(Binder binder)
	{
		binder.bind(AssetManager.class).to(AssetManagerImpl.class);
	}
	
	@Contribution(name="asset-protect")
	public void contributeDefaultProtectedExtensions(AssetManager manager)
	{
		manager.addProtectedExtension("xml");
		manager.addProtectedExtension("class");
	}
	
	@Contribution(name="asset-page")
	public void contributeAssetPage(PageManager manager)
	{
		manager.add(AssetProvider.class);
	}
	
	@Contribution(name="asset-property-source")
	public void contributeAssetPropertySource(TemplateManager manager)
	{
		manager.addPropertySource("asset", AssetPropertySource.class);
		manager.addPropertySource("a", AssetPropertySource.class);
	}
	
	@Contribution(name="asset-sources")
	public void contributeClasspathSource(AssetManager manager)
	{
		manager.addSource(ClasspathAssetSource.class);
		manager.addSource(ContextAssetSource.class);
	}
	
	@Contribution(name="asset-provider")
	public void contributeDefaultMessageProviders(ResteasyProviderFactory factory,
			AssetWriter writer)
	{
		factory.addMessageBodyWriter(writer);
	}
}
