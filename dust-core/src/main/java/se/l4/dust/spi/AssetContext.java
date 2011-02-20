package se.l4.dust.spi;

import java.net.URI;

import se.l4.dust.api.asset.Asset;

/**
 * Context for assets, used by template emitters to resolve assets to the
 * correct location.
 * 
 * @author andreas
 *
 */
public interface AssetContext
{
	/**
	 * Generate a URI for the given asset.
	 * 
	 * @param asset
	 * @return
	 */
	URI generateURI(Asset asset);
}
