package se.l4.dust.api.messages;

import java.io.IOException;

import se.l4.dust.api.Context;
import se.l4.dust.api.resource.ResourceLocation;

/**
 * Source of data for {@link MessageCollection}.
 *
 * @author Andreas Holstenson
 *
 */
public interface MessageSource
{
	/**
	 * Attempt to load any message besides the given resource. The URL will
	 * be the location of a resource, such as a template.
	 *
	 * @param context
	 * 		the current context
	 * @param resource
	 * 		the resource URL
	 * @return
	 */
	MessageCollection load(Context context, ResourceLocation resource)
		throws IOException;
}
