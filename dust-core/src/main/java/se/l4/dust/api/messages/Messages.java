package se.l4.dust.api.messages;

import se.l4.dust.api.resource.variant.ResourceVariant;

/**
 * Support for messages.
 * 
 * @author Andreas Holstenson
 *
 */
public interface Messages
{
	/**
	 * Get the given property.
	 * 
	 * @param property
	 * @return
	 */
	String get(String property);
	
	/**
	 * Get the {@link ResourceVariant} of the messages. Used within templates.
	 * 
	 * @return
	 */
	ResourceVariant getVariant();
}
