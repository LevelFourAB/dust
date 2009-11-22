package se.l4.dust.api.asset;

import java.net.URL;

import org.jdom.Namespace;

public interface Asset
{
	Namespace getNamespace();
	
	String getName();
	
	String getChecksum();
	
	URL getURL();
}
