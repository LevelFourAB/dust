package se.l4.dust.api.asset;

import java.net.URL;

import org.jdom.Namespace;

/**
 * Source of assets, used to support pluggable asset implementations.
 * 
 * @author Andreas Holstenson
 *
 */
public interface AssetSource
{
	/**
	 * Attempt to locate a file within a given namespace, returning {@code null}
	 * if no such asset could be found.
	 *  
	 * @param ns
	 * 		namespace of asset
	 * @param pathToFile
	 * 		path to asset
	 * @return
	 * 		URL pointing to asset if found, otherwise {@code null}
	 */
	URL locate(Namespace ns, String pathToFile);
}
