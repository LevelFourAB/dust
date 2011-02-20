package se.l4.dust.jaxrs.internal;

import se.l4.dust.jaxrs.PageManager;
import se.l4.dust.jaxrs.PageProvider;
import se.l4.dust.jaxrs.spi.Configuration;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;


@Singleton
public class NormalPageManager
	implements PageManager
{
	private final Configuration config;
	private final Injector injector;
	
	@Inject
	public NormalPageManager(Configuration config, Injector injector)
	{
		this.config = config;
		this.injector = injector;
	}
	
	public void add(final Class<?> page)
	{
		config.addPage(new PageProvider()
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
