package se.l4.dust.core.internal;

import se.l4.dust.api.PageManager;
import se.l4.dust.api.PageProvider;
import se.l4.dust.api.PageProviderManager;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;


@Singleton
public class NormalPageManager
	implements PageManager
{
	private final PageProviderManager provider;
	private final Injector injector;
	
	@Inject
	public NormalPageManager(PageProviderManager provider, Injector injector)
	{
		this.provider = provider;
		this.injector = injector;
	}
	
	public void add(final Class<?> page)
	{
		provider.add(new PageProvider()
		{
			public Object get()
			{
				return injector.getInstance(page);
			}
			
			public Class<?> getType()
			{
				return page;
			}
			
			public void release(Object o)
			{
			}
		});
	}
	
	
}
