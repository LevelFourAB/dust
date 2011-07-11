package se.l4.dust.api.asset;

import java.io.IOException;

import se.l4.dust.api.resource.Resource;

/**
 * Processor of an asset, used to modify the asset data in runtime before
 * returning it to the client.
 * 
 * @author Andreas Holstenson
 *
 */
public interface AssetProcessor
{
	/**
	 * Process a resource belonging to the given namespace and path returning
	 * a new resource with the processed asset.
	 * 
	 * @param namespace
	 * 		namespace of the asset
	 * @param path
	 * 		path within namespace to the asset
	 * @param in
	 * 		resource with asset data
	 * @return
	 * 		resource with processed data
	 * @throws IOException
	 * 		if unable to process the stream
	 */
	Resource process(String namespace, String path, Resource in, Object... arguments)
		throws IOException;
}
