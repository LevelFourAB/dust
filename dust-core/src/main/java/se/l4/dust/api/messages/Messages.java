package se.l4.dust.api.messages;

import se.l4.dust.api.Context;

/**
 * Manager of {@link MessageCollection messages}.
 * 
 * @author Andreas Holstenson
 *
 */
public interface Messages
{
	/**
	 * Get the messages for the given URL and context.
	 * 
	 * @param context
	 * @param url
	 * @return
	 */
	MessageCollection getMessages(Context context, String url);
	
	/**
	 * Get messages for the given type and context.
	 * 
	 * @param context
	 * @param type
	 * @return
	 */
	MessageCollection getMessages(Context context, Class<?> type);
	
	/**
	 * Add a new source to the manager.
	 * 
	 * @param source
	 */
	void addSource(MessageSource source);
}
