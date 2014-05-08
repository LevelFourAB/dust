package se.l4.dust.core.internal.messages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import se.l4.dust.api.messages.Messages;
import se.l4.dust.api.resource.variant.ResourceVariant;
import se.l4.dust.core.internal.resource.MergedResourceVariant;

/**
 * Implementation of {@link Messages} that delegates everything to other
 * implementations.
 * 
 * @author Andreas Holstenson
 *
 */
public class DelegatingMessages
	implements Messages
{
	private final Messages[] other;

	public DelegatingMessages(List<Messages> others)
	{
		this.other = others.toArray(new Messages[others.size()]);
	}

	@Override
	public String get(String property)
	{
		for(Messages m : other)
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
