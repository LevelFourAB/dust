package se.l4.dust.core.internal.messages;

import java.util.ArrayList;
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
	private final ResourceVariant variant;

	public DelegatingMessages(List<Messages> others)
	{
		this.other = others.toArray(new Messages[others.size()]);
		
		List<ResourceVariant> variants = new ArrayList<ResourceVariant>();
		for(Messages m : this.other)
		{
			if(m.getVariant() != null)
			{
				variants.add(m.getVariant());
			}
		}
		
		this.variant = variants.isEmpty() ? null : 
			new MergedResourceVariant(variants.toArray(new ResourceVariant[variants.size()]));
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
	public ResourceVariant getVariant()
	{
		return variant;
	}
}
