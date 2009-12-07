package se.l4.dust.core.internal.asset;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.ws.rs.core.UriBuilder;

import org.apache.commons.codec.binary.Hex;
import org.jdom.Namespace;

import se.l4.dust.api.NamespaceManager;
import se.l4.dust.api.asset.Asset;
import se.l4.dust.api.asset.Resource;

public class AssetImpl
	implements Asset
{
	private final Namespace ns;
	private final String name;
	private final Resource resource;
	private final String checksum;
	private final URI uri;
	
	public AssetImpl(NamespaceManager manager, boolean protect, Namespace ns, String name, Resource resource)
	{
		if(manager != null && resource == null)
		{
			// Check manager for null assets
			throw new IllegalArgumentException("Resource of asset can not be null (name " + name + " in " + ns + ")");
		}
		
		this.ns = ns;
		this.name = name;
		this.resource = resource;
		
		String checksum = "";
		URI uri = null;
		if(ns != null)
		{
			Namespace nns = manager.getNamespaceByURI(ns.getURI());
			if(nns == null)
			{
				throw new RuntimeException("Namespace " + ns.getURI() + " is not bound to NamespaceManager");
			}
			
			String prefix = nns.getPrefix();
			if(protect)
			{
				int idx = name.lastIndexOf('.');
				String extension = name.substring(idx + 1);
				checksum = getChecksum(resource);

				name = name.substring(0, idx) + "." + checksum + "." + extension; 
			}
			
			String version = manager.getVersion(nns);
			
			UriBuilder builder = UriBuilder.fromPath("/asset/{ns}/{version}")
				.path(name);
			
			uri = builder.build(prefix, version);
		}
		
		this.checksum = checksum;
		this.uri = uri;
	}
	
	private String getChecksum(Resource resource)
	{
		try
		{
			byte[] digest = digest(resource);
			return new String(Hex.encodeHex(digest));
		}
		catch(NoSuchAlgorithmException e)
		{
		}
		catch(DigestException e)
		{
		}
		catch(IOException e)
		{
		}
		
		throw new RuntimeException("Unable to access resource " + resource);
	}
	
	private byte[] digest(Resource resource)
		throws IOException, NoSuchAlgorithmException, DigestException
	{
		InputStream stream = resource.openStream();
		try
		{
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			byte[] buf = new byte[1024];
			int len;
			while((len = stream.read(buf)) != -1)
			{
				digest.update(buf, 0, len);
			}
			
			return digest.digest();
		}
		finally
		{
			stream.close();
		}
	}
	
	public String getChecksum()
	{
		return checksum;
	}

	public String getName()
	{
		return name;
	}

	public Namespace getNamespace()
	{
		return ns;
	}

	public Resource getResource()
	{
		return resource;
	}

	@Override
	public String toString()
	{
		return uri.toString();
	}
}
