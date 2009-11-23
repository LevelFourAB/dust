package se.l4.dust.core.internal.asset;

import org.jboss.resteasy.spi.ResteasyProviderFactory;

import se.l4.crayon.annotation.Contribution;
import se.l4.crayon.annotation.Description;
import se.l4.dust.api.PageManager;
import se.l4.dust.api.TemplateManager;
import se.l4.dust.api.asset.AssetManager;
import se.l4.dust.core.asset.AssetProvider;

import com.google.inject.Binder;

public class AssetModule
{
	@Description(name="asset")
	public void configure(Binder binder)
	{
		binder.bind(AssetManager.class).to(AssetManagerImpl.class);
	}
	
	@Contribution(name="asset-page")
	public void contributeAssetPage(PageManager manager)
	{
		manager.add(AssetProvider.class);
	}
	
	@Contribution(name="asset-property-source")
	public void contributeAssetPropertySource(TemplateManager manager,
			AssetPropertySource source)
	{
		manager.addPropertySource("asset", source);
		manager.addPropertySource("a", source);
	}
	
	@Contribution(name="asset-classpath")
	public void contributeClasspathSource(
			AssetManager manager,
			ClasspathAssetSource source)
	{
		manager.addSource(source);
	}
	
	@Contribution(name="asset-provider")
	public void contributeDefaultMessageProviders(ResteasyProviderFactory factory,
			AssetWriter writer)
	{
		factory.addMessageBodyWriter(writer);
	}
}
