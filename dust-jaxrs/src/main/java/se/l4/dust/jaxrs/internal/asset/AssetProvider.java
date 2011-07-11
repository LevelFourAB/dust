package se.l4.dust.jaxrs.internal.asset;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.jdom.Namespace;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import se.l4.dust.api.NamespaceManager;
import se.l4.dust.api.asset.Asset;
import se.l4.dust.api.asset.AssetManager;

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
	@Path("{ns}/{version}/{path:.+}")
	public Object serve(
			@PathParam("ns") String prefix, 
			@PathParam("version") String version, 
			@PathParam("path") String path)
	{
		Namespace ns = namespaces.getNamespaceByPrefix(prefix);
		if(ns == null)
		{
			return Response.status(404).build();
		}
		
		int idx = path.lastIndexOf('.');
		String checksum = null;
		if(idx >= 0)
		{
			// Check extension to get if we need checksum
			String extension = path.substring(idx + 1);
			if(manager.isProtectedExtension(extension))
			{
				int idx2 = path.lastIndexOf('.', idx-1);
				checksum = path.substring(idx2+1, idx);
				path = path.substring(0, idx2) + "." + extension;
			}
		}
		
		Asset a = manager.locate(ns.getURI(), path);
		if(a == null)
		{
			return Response.status(404).build();
		}
		
		if(checksum != null && false == checksum.equals(a.getChecksum()))
		{
			return Response.status(404).build();
		}
		
		return a;
	}
}
