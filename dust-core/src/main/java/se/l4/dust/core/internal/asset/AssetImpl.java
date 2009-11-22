package se.l4.dust.core.internal.asset;

import java.net.URL;

import org.jdom.Namespace;

import se.l4.dust.api.asset.Asset;

public class AssetImpl
	implements Asset
{

	private final Namespace ns;
	private final String name;
	private final URL url;
	private final String checksum;
	
	public AssetImpl(Namespace ns, String name, URL url)
	{
		this.ns = ns;
		this.name = name;
		this.url = url;
		
		checksum = "";
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

	public URL getURL()
	{
		return url;
	}

}
