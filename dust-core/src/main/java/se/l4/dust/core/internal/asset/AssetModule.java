package se.l4.dust.core.internal.asset;

import java.lang.reflect.Field;

import org.jdom.Namespace;

import se.l4.crayon.CrayonModule;
import se.l4.crayon.annotation.Contribution;
import se.l4.dust.api.TemplateManager;
import se.l4.dust.api.annotation.InjectAsset;
import se.l4.dust.api.asset.Asset;
import se.l4.dust.api.asset.AssetManager;

import com.google.inject.MembersInjector;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

public class AssetModule
	extends CrayonModule
{
	@Override
	public void configure()
	{
		bind(AssetManager.class).to(AssetManagerImpl.class);
		
		bindListener(Matchers.any(), new TypeListener()
		{
			public <I> void hear(TypeLiteral<I> typeLiteral, TypeEncounter<I> typeEncounter)
			{
				Provider<AssetManager> assets = typeEncounter.getProvider(AssetManager.class);
				for(Field field : typeLiteral.getRawType().getDeclaredFields())
				{
					if(field.isAnnotationPresent(InjectAsset.class))
					{
						if(field.getType().isAssignableFrom(Asset.class))
						{
							typeEncounter.register(new AssetInjector<I>(assets, field));
						}
						else
						{
							typeEncounter.addError("Type of field %s is not compatible with Asset", field);
						}
					}
				}
			}
		});
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
	
	private static class AssetInjector<T>
		implements MembersInjector<T>
	{
		private final Provider<AssetManager> assets;
		private final Field field;

		public AssetInjector(Provider<AssetManager> assets, Field field)
		{
			this.assets = assets;
			this.field = field;
			
			field.setAccessible(true);
		}
		
		public void injectMembers(T instance)
		{
			InjectAsset annotation = field.getAnnotation(InjectAsset.class);
			String ns = annotation.namespace();
			String path = annotation.path();
			
			AssetManager manager = assets.get();

			Asset asset = manager.locate(Namespace.getNamespace(ns), path);
			if(asset == null)
			{
				throw new ProvisionException("Unable to locate asset named " + path + " in namespace " + ns);
			}
			
			try
			{
				field.set(instance, asset);
			}
			catch(Exception e)
			{
			}
		}
	}
}
