package se.l4.dust.core.internal.asset;

import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

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
	private static final Asset NULL_ASSET = new AssetImpl(null, false, null, null, null);
	
	private final ConcurrentMap<Namespace, ConcurrentMap<String, Asset>> cache;
	private final NamespaceManager manager;
	private final List<AssetSource> sources;
	private final Set<String> protectedExtensions;
	
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
		
		protectedExtensions = new CopyOnWriteArraySet<String>();
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

	public void addProtectedExtension(String extension)
	{
		protectedExtensions.add(extension);
	}
	
	public boolean isProtectedExtension(String extension)
	{
		return protectedExtensions.contains(extension);
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
			int idx = from.lastIndexOf('.');
			String extension = idx > 0 ? from.substring(idx+1) : "";
			boolean protect = isProtectedExtension(extension);
			
			for(AssetSource source : sources)
			{
				URL url = source.locate(namespace, from);
				if(url != null)
				{
					return new AssetImpl(manager, protect, namespace, from, url);
				}
			}
			
			return NULL_ASSET;
		}
	}
}
