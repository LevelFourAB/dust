package se.l4.dust.api.resource;

import java.io.IOException;
import java.io.InputStream;

/**
 * Resource abstraction, used to enable loading of resources from any source.
 *
 * @author Andreas Holstenson
 *
 */
public interface Resource
{
	/**
	 * Get the location of this resource.
	 *
	 * @return
	 */
	ResourceLocation getLocation();

	/**
	 * Get the content type of the resource, {@code null} if unknown.
	 *
	 * @return
	 */
	String getContentType();

	/**
	 * Get the length of the content, or {@code -1} if length is unknown.
	 *
	 * @return
	 */
	int getContentLength();

	/**
	 * Get the content encoding of the resource or {@code null} if unknown.
	 *
	 * @return
	 */
	String getContentEncoding();

	/**
	 * Get when the resource was last modified or {@code -1} if unknown.
	 *
	 * @return
	 */
	long getLastModified();

	/**
	 * Open a stream with the contents of the resource.
	 *
	 * @return
	 * @throws IOException
	 */
	InputStream openStream()
		throws IOException;
}
