package se.l4.dust.core.internal.resource;

import java.util.Arrays;

import com.google.common.base.MoreObjects;

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

	@Override
	public Object getCacheValue()
	{
		Object[] result = new Object[variants.length];
		for(int i=0, n=variants.length; i<n; i++)
		{
			result[i] = variants[i].getCacheValue();
		}
		return result;
	}

	@Override
	public String getIdentifier()
	{
		return id;
	}

	@Override
	public boolean isMoreSpecific(ResourceVariant current)
	{
		return false;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(variants);
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		MergedResourceVariant other = (MergedResourceVariant) obj;
		if(!Arrays.equals(variants, other.variants))
			return false;
		return true;
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

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(this)
			.add("id", id)
			.add("variants", Arrays.toString(variants))
			.toString();
	}
}
