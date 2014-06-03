package se.l4.dust.core.internal.resource;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import se.l4.dust.api.resource.NamespaceLocation;
import se.l4.dust.api.resource.Resource;
import se.l4.dust.api.resource.ResourceLocation;
import se.l4.dust.api.resource.ResourceLocator;
import se.l4.dust.api.resource.Resources;
import se.l4.dust.api.resource.UrlLocation;
import se.l4.dust.api.resource.UrlResource;

import com.google.inject.Singleton;

@Singleton
public class ResourcesImpl
	implements Resources
{
	private final List<ResourceLocator> sources;
	
	public ResourcesImpl()
	{
		sources = new CopyOnWriteArrayList<>();
	}
	
	@Override
	public void addLocator(ResourceLocator source)
	{
		sources.add(source);
	}

	@Override
	public Resource locate(String namespace, String file)
		throws IOException
	{
		for(ResourceLocator locator : sources)
		{
			Resource resource = locator.locate(namespace, file);
			if(resource != null) return resource;
		}
		
		return null;
	}
	
	@Override
	public Resource locate(URL url)
		throws IOException
	{
		try
		{
			return new UrlResource(new UrlLocation(url), url);
		}
		catch(IOException e)
		{
			return null;
		}
	}
	
	@Override
	public Resource locate(ResourceLocation location)
		throws IOException
	{
		if(location instanceof NamespaceLocation)
		{
			NamespaceLocation nl = (NamespaceLocation) location;
			return locate(nl.getNamespace().getUri(), nl.getName());
		}
		else if(location instanceof UrlLocation)
		{
			return locate(((UrlLocation) location).getUrl());
		}
		
		return null;
	}
}
