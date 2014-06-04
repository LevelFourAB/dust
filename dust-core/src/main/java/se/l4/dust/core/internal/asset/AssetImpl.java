package se.l4.dust.core.internal.asset;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import se.l4.dust.api.Namespace;
import se.l4.dust.api.asset.Asset;
import se.l4.dust.api.resource.Resource;

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
	
	public AssetImpl(Namespace ns, String name, Resource resource, boolean protect)
	{
		this.ns = ns;
		this.name = name;
		
		this.resource = resource;
		this.protect = protect;
		
		this.checksum = protect ? createChecksum() : null;
	}
	
	private String createChecksum()
	{
		Resource resource = getResource();
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
	
	@Override
	public String getChecksum()
	{
		return checksum;
	}
	
	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public Namespace getNamespace()
	{
		return ns;
	}

	@Override
	public Resource getResource()
	{
		return resource;
	}
	
	@Override
	public boolean isProtected()
	{
		return protect;
	}
}
