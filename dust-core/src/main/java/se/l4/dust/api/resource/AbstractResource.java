package se.l4.dust.api.resource;


public abstract class AbstractResource
	implements Resource
{
	private final ResourceLocation location;

	public AbstractResource(ResourceLocation location)
	{
		this.location = location;
	}

	@Override
	public ResourceLocation getLocation()
	{
		return location;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((location == null) ? 0 : location.hashCode());
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
		AbstractResource other = (AbstractResource) obj;
		if(location == null)
		{
			if(other.location != null)
				return false;
		}
		else if(!location.equals(other.location))
			return false;
		return true;
	}
}
