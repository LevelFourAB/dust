package se.l4.dust.api.messages;

import se.l4.dust.api.resource.variant.ResourceVariant;

/**
 * Abstract implementation of {@link Messages}. Should be used by 
 * {@link MessageSource}.
 * 
 * @author Andreas Holstenson
 *
 */
public abstract class AbstractMessages
	implements Messages
{
	private final ResourceVariant variant;

	public AbstractMessages(ResourceVariant variant)
	{
		this.variant = variant;
	}
	
	@Override
	public ResourceVariant getVariant()
	{
		return variant;
	}
}
