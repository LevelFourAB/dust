package se.l4.dust.api.messages;

import java.util.Set;

/**
 * Support for messages.
 *
 * @author Andreas Holstenson
 *
 */
public interface MessageCollection
{
	/**
	 * Get the given property.
	 *
	 * @param property
	 * @return
	 */
	String get(String property);

	/**
	 * Get all of the available message keys.
	 *
	 * @return
	 */
	Set<String> keys();
}
