package se.l4.dust.jaxrs.internal.asset;

import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import se.l4.crayon.Environment;
import se.l4.dust.api.Context;
import se.l4.dust.api.NamespaceManager;
import se.l4.dust.api.asset.Asset;
import se.l4.dust.api.asset.AssetManager;
import se.l4.dust.api.resource.Resource;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
@Path("asset")
public class AssetProvider
{
	private final AssetManager manager;
	private final NamespaceManager namespaces;
	private final Context context;
	
	@Inject
	public AssetProvider(NamespaceManager namespaces, AssetManager manager,
			Environment environment)
	{
		this.namespaces = namespaces;
		this.manager = manager;
		
		context = new Context()
		{
			public void putValue(Object key, Object value)
			{
			}
			
			public <T> T getValue(Object key)
			{
				return null;
			}
		};
	}
	
	@HEAD
	@Path("{ns}/{version}/{path:.+}")
	public Object head(
			@PathParam("ns") String prefix, 
			@PathParam("version") String version, 
			@PathParam("path") String path)
	{
		Object result = serve(prefix, version, path);
		if(result instanceof Asset)
		{
			Asset asset = (Asset) result;
			Resource resource = asset.getResource();
			
			return Response.ok()
				.lastModified(new Date(resource.getLastModified()))
				.type(AssetWriter.getMimeType(asset))
				.build();
		}
		
		return result;
	}
	
	@GET
	@Path("{ns}/{version}/{path:.+}")
	public Object serve(
			@PathParam("ns") String prefix, 
			@PathParam("version") String version, 
			@PathParam("path") String path)
	{
		NamespaceManager.Namespace ns = namespaces.getNamespaceByPrefix(prefix);
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
		
		Asset a = manager.locate(context, ns.getUri(), path);
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
