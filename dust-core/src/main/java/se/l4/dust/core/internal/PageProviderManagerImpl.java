package se.l4.dust.core.internal;

import org.jboss.resteasy.spi.Registry;

import com.google.inject.Inject;

import se.l4.dust.api.PageProvider;
import se.l4.dust.api.PageProviderManager;


public class PageProviderManagerImpl
	implements PageProviderManager
{
	private final Registry registry;
	
	@Inject
	public PageProviderManagerImpl(Registry registry)
	{
		this.registry = registry;
	}
	
	public void add(PageProvider factory)
	{
		registry.addResourceFactory(new PageResourceFactory(factory));
	}

	public void remove(PageProvider factory)
	{
		// TODO: IMPLEMENT
	}

}
