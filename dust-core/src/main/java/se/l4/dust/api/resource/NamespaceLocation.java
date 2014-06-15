package se.l4.dust.api.resource;

import java.net.URI;

import se.l4.dust.api.Namespace;

public class NamespaceLocation
	implements ResourceLocation
{
	private final Namespace namespace;
	private final String name;

	public NamespaceLocation(Namespace namespace, String name)
	{
		this.namespace = namespace;
		this.name = name;
	}
	
	public Namespace getNamespace()
	{
		return namespace;
	}
	
	@Override
	public String getName()
	{
		return name;
	}
	
	@Override
	public ResourceLocation withExtension(String newExtension)
	{
		int idx = name.lastIndexOf('.');
		String firstPart = idx > 0 ? name.substring(0, idx) : name;
		return new NamespaceLocation(namespace, firstPart + "." + newExtension);
	}
	
	@Override
	public ResourceLocation resolve(String path)
	{
		String newName = URI.create(name).resolve(path).toString();
		return new NamespaceLocation(namespace, newName);
	}
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "{namespace=" + namespace.getUri() + ", name=" + name + "}";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((namespace == null) ? 0 : namespace.hashCode());
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
		NamespaceLocation other = (NamespaceLocation) obj;
		if(name == null)
		{
			if(other.name != null)
				return false;
		}
		else if(!name.equals(other.name))
			return false;
		if(namespace == null)
		{
			if(other.namespace != null)
				return false;
		}
		else if(!namespace.equals(other.namespace))
			return false;
		return true;
	}
}
