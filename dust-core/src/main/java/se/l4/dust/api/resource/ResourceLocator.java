package se.l4.dust.api.resource;

import java.io.IOException;

/**
 * Source of assets, used to support pluggable asset implementations.
 *
 * @author Andreas Holstenson
 *
 */
public interface ResourceLocator
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
	Resource locate(String ns, String pathToFile)
		throws IOException;


}
