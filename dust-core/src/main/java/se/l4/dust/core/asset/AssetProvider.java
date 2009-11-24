package se.l4.dust.core.asset;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.annotations.GZIP;
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
	@GZIP
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
		
		Asset a = manager.locate(ns, path);
		if(a == null/* || false == checksum.equals(a.getChecksum())*/)
		{
			return Response.status(404).build();
		}
		
		return a;
	}
}
