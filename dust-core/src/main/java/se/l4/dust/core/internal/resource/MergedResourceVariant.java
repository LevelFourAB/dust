package se.l4.dust.core.internal.resource;

import se.l4.dust.api.resource.variant.ResourceVariant;

/**
 * Merged resource variant.
 * 
 * @author Andreas Holstenson
 *
 */
public class MergedResourceVariant
	implements ResourceVariant
{
	private final ResourceVariant[] variants;
	private final String id;

	public MergedResourceVariant(ResourceVariant... variants)
	{
		this.variants = variants;
		StringBuilder id = new StringBuilder();
		for(int i=0, n=variants.length; i<n; i++)
		{
			if(i > 0)
			{
				id.append('.');
			}
			
			id.append(variants[i].getIdentifier());
		}
		
		this.id = id.toString();
	}

	public Object getCacheValue()
	{
		Object[] result = new Object[variants.length];
		for(int i=0, n=variants.length; i<n; i++)
		{
			result[i] = variants[i].getCacheValue();
		}
		return result;
	}

	public String getIdentifier()
	{
		return id;
	}
	
	public boolean hasSpecific(ResourceVariant other)
	{
		if(other instanceof MergedResourceVariant)
		{
			for(ResourceVariant v : ((MergedResourceVariant) other).variants)
			{
				if(hasSpecific(v))
				{
					return true;
				}
			}
		}
		else
		{
			for(ResourceVariant v : variants)
			{
				if(v.getClass().isAssignableFrom(other.getClass()))
				{
					if(other.getIdentifier().equals(v.getIdentifier()))
					{
						return true;
					}
				}
			}
		}
		
		return false;
	}
}
