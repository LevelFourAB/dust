package se.l4.dust.core.internal.messages;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

import se.l4.dust.api.messages.MessageCollection;

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
	public Set<String> keys()
	{
		Set<String> allKeys = Sets.newHashSet();
		for(MessageCollection m : other)
		{
			allKeys.addAll(m.keys());
		}
		return allKeys;
	}
	
	@Override
	public String toString()
	{
		return "DelegatingMessages{other=" + Arrays.toString(other) + "}";
	}
}
