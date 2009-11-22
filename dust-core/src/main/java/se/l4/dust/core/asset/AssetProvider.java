package se.l4.dust.core.asset;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.jdom.Namespace;

import se.l4.dust.api.NamespaceManager;
import se.l4.dust.api.asset.Asset;
import se.l4.dust.api.asset.AssetManager;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
@Path("asset")
public class AssetProvider
{
	private final AssetManager manager;
	private final NamespaceManager namespaces;
	
	@Inject
	public AssetProvider(NamespaceManager namespaces, AssetManager manager)
	{
		this.namespaces = namespaces;
		this.manager = manager;
	}
	
	@GET
	@Path("{ns}/{path:.+}")
	public Object serve(@PathParam("ns") String prefix, @PathParam("path") String path)
	{
		Namespace ns = namespaces.getNamespaceByPrefix(prefix);
		if(ns == null)
		{
			return null;
		}
		
		Asset a = manager.locate(ns, path);
		return a == null ? null : a.getURL();
	}
}
