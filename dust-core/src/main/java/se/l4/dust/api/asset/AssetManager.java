package se.l4.dust.api.asset;

import org.jdom.Namespace;

public interface AssetManager
{
	/**
	 * Attempt to locate the given asset by traversing all of the registered
	 * sources and returning the first match.
	 *  
	 * @param ns
	 * @param file
	 * @return
	 */
	Asset locate(Namespace ns, String file);
	
	/**
	 * Add a source of assets to the manager.
	 * 
	 * @param source
	 */
	void addSource(AssetSource source);
}
