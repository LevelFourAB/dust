package se.l4.dust.api.asset;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Cache interface for asset files, can be tied up the application using
 * Dust to enable caching for asset transformations.
 *
 * @author Andreas Holstenson
 *
 */
public interface AssetCache
{
	/**
	 * Get an output stream to use for storing a file the given name. The
	 * file use a forward slash (/) as its separator.
	 *
	 * @param name
	 * @return
	 */
	OutputStream store(String name)
		throws IOException;

	/**
	 * Get an object from the cache.
	 *
	 * @param name
	 * @return
	 */
	InputStream get(String name)
		throws IOException;
}
