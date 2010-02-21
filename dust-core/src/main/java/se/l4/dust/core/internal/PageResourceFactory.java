package se.l4.dust.core.internal;

import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.resteasy.spi.InjectorFactory;
import org.jboss.resteasy.spi.PropertyInjector;
import org.jboss.resteasy.spi.ResourceFactory;

import se.l4.dust.api.PageProvider;

/**
 * {@link ResourceFactory} that creates resources for RESTeasy based on 
 * {@link PageProvider}s. This is how we can use Guice for creation of pages,
 * which allows our pages to have different scopes.
 * 
 * @author Andreas Holstenson
 *
 */
public class PageResourceFactory
	implements ResourceFactory
{
	private final PageProvider provider;
	private PropertyInjector propertyInjector;
	
	public PageResourceFactory(PageProvider provider)
	{
		this.provider = provider;
	}
	
	public Object createResource(HttpRequest request, HttpResponse response,
		InjectorFactory factory)
	{
		Object o = provider.get();
		
		// Inject properties
		propertyInjector.inject(request, response, o);
		
		return o;
	}

	public Class<?> getScannableClass()
	{
		return provider.getType();
	}

	public void registered(InjectorFactory factory)
	{
		this.propertyInjector = factory.createPropertyInjector(provider.getType());
	}

	public void requestFinished(HttpRequest request, HttpResponse response, Object resource)
	{
		provider.release(resource);
	}

	public void unregistered()
	{
	}

}
