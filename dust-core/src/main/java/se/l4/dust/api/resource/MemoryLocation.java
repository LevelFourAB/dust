package se.l4.dust.api.resource;

public class MemoryLocation
	implements ResourceLocation
{
	private final String name;
	
	public MemoryLocation(String name)
	{
		this.name = name;
	}

	@Override
	public ResourceLocation withExtension(String newExtension)
	{
		return null;
	}

	@Override
	public String getName()
	{
		return name;
	}

}
