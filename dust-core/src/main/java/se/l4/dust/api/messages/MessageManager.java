package se.l4.dust.api.messages;

import se.l4.dust.api.Context;

/**
 * Manager of {@link Messages messages}.
 * 
 * @author Andreas Holstenson
 *
 */
public interface MessageManager
{
	/**
	 * Get the messages for the given URL and context.
	 * 
	 * @param context
	 * @param url
	 * @return
	 */
	Messages getMessages(Context context, String url);
	
	/**
	 * Add a new source to the manager.
	 * 
	 * @param source
	 */
	void addSource(MessageSource source);
}
