package se.l4.dust.core.internal.resource;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import se.l4.dust.api.resource.Resource;
import se.l4.dust.api.resource.ResourceLocator;
import se.l4.dust.api.resource.Resources;

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
}
