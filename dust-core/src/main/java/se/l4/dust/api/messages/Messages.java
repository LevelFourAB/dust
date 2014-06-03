package se.l4.dust.api.messages;

import se.l4.dust.api.Context;
import se.l4.dust.api.resource.ResourceLocation;

/**
 * Manager of {@link MessageCollection messages}.
 * 
 * @author Andreas Holstenson
 *
 */
public interface Messages
{
	/**
	 * Get the messages for the given location and context.
	 * 
	 * @param context
	 * @param url
	 * @return
	 */
	MessageCollection getMessages(Context context, ResourceLocation location);
	
	/**
	 * Add a new source to the manager.
	 * 
	 * @param source
	 */
	void addSource(MessageSource source);
}
