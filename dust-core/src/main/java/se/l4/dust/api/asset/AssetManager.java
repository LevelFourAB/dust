package se.l4.dust.api.asset;

import org.jdom.Namespace;

public interface AssetManager
{
	Asset locate(Namespace ns, String file);
}
