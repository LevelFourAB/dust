package se.l4.dust.core.internal.asset;

import java.net.URL;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jdom.Namespace;

import se.l4.dust.api.NamespaceManager;
import se.l4.dust.api.asset.Asset;
import se.l4.dust.api.asset.AssetManager;
import se.l4.dust.api.asset.AssetSource;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class AssetManagerImpl
	implements AssetManager
{
	private static final Asset NULL_ASSET = new AssetImpl(null, null, null, null);
	
	private final ConcurrentMap<Namespace, ConcurrentMap<String, Asset>> cache;
	private final NamespaceManager manager;
	private final List<AssetSource> sources;
	
	@Inject
	public AssetManagerImpl(NamespaceManager manager)
	{
		this.manager = manager;
		sources = new CopyOnWriteArrayList<AssetSource>();
		
		cache = new MapMaker()
			.makeComputingMap(new Function<Namespace, ConcurrentMap<String, Asset>>()
			{
				public ConcurrentMap<String, Asset> apply(Namespace from)
				{
					return new MapMaker().makeComputingMap(new AssetLocator(from));
				}
			});
	}
	
	public void addSource(AssetSource source)
	{
		sources.add(source);
	}
	
	public Asset locate(Namespace ns, String file)
	{
		ConcurrentMap<String, Asset> map = cache.get(ns);
		Asset a = map.get(file);
		
		return a == NULL_ASSET ? null: a;
	}

	private class AssetLocator
		implements Function<String, Asset>
	{
		private final Namespace namespace;

		public AssetLocator(Namespace namespace)
		{
			this.namespace = namespace;
		}
		
		public Asset apply(String from)
		{
			for(AssetSource source : sources)
			{
				URL url = source.locate(namespace, from);
				if(url != null)
				{
					return new AssetImpl(manager, namespace, from, url);
				}
			}
			
			return NULL_ASSET;
		}
	}
}
