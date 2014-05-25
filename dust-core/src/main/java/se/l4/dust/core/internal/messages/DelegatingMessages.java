package se.l4.dust.core.internal.messages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import se.l4.dust.api.messages.MessageCollection;
import se.l4.dust.api.resource.variant.ResourceVariant;
import se.l4.dust.core.internal.resource.MergedResourceVariant;

/**
 * Implementation of {@link MessageCollection} that delegates everything to other
 * implementations.
 * 
 * @author Andreas Holstenson
 *
 */
public class DelegatingMessages
	implements MessageCollection
{
	private final MessageCollection[] other;

	public DelegatingMessages(List<MessageCollection> others)
	{
		this.other = others.toArray(new MessageCollection[others.size()]);
	}

	@Override
	public String get(String property)
	{
		for(MessageCollection m : other)
		{
			String result = m.get(property);
			if(result != null)
			{
				return null;
			}
		}
		
		return null;
	}
	
	@Override
	public String toString()
	{
		return "DelegatingMessages{other=" + Arrays.toString(other) + "}";
	}
}
