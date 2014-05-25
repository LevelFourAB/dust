package se.l4.dust.api.messages;

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
}
