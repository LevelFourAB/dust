package se.l4.dust.core.internal.asset;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.jdom.Namespace;

import se.l4.dust.api.NamespaceManager;
import se.l4.dust.api.asset.Asset;
import se.l4.dust.api.asset.Resource;

/**
 * Implementation of {@link Asset}.
 * 
 * @author Andreas Holstenson
 *
 */
public class AssetImpl
	implements Asset
{
	private final Namespace ns;
	private final String name;
	private final Resource resource;
	private final String checksum;
	private final boolean protect;
	
	public AssetImpl(NamespaceManager manager, boolean protect, Namespace ns, String name, Resource resource)
	{
		this.protect = protect;
		if(manager != null && resource == null)
		{
			// Check manager for null assets
			throw new IllegalArgumentException("Resource of asset can not be null (name " + name + " in " + ns + ")");
		}
		
		this.ns = ns;
		this.name = name;
		this.resource = resource;
		
		this.checksum = resource == null ? null : getChecksum(resource);
	}
	
	private String getChecksum(Resource resource)
	{
		try
		{
			byte[] digest = digest(resource);
			return toHex(digest);
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
	
	private static final String HEX = "0123456789abcdef";
	
	private static String toHex(byte[] data)
	{
		StringBuilder result = new StringBuilder(data.length * 2);
		for(byte b: data)
		{
			result
				.append(HEX.charAt((b & 0xF0) >> 4))
				.append(HEX.charAt(b & 0x0F));
		}
		
		return result.toString();
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
	
	public boolean isProtected()
	{
		return protect;
	}
}
