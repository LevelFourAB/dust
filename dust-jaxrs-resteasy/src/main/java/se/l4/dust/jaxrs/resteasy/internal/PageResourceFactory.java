package se.l4.dust.jaxrs.resteasy.internal;

import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.resteasy.spi.PropertyInjector;
import org.jboss.resteasy.spi.ResourceFactory;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import com.google.inject.Provider;

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
	private final Provider<Object> provider;
	private final Class<?> type;

	private PropertyInjector propertyInjector;

	public PageResourceFactory(Provider<Object> provider, Class<?> type)
	{
		this.provider = provider;
		this.type = type;
	}

	public Object createResource(HttpRequest request, HttpResponse response,
			ResteasyProviderFactory factory)
	{
		Object o = provider.get();

		// Inject properties
		propertyInjector.inject(request, response, o);

		return o;
	}

	public Class<?> getScannableClass()
	{
		return type;
	}

	public void registered(ResteasyProviderFactory factory)
	{
		this.propertyInjector = factory.getInjectorFactory().createPropertyInjector(type, factory);
	}

	public void requestFinished(HttpRequest request, HttpResponse response, Object resource)
	{
	}

	public void unregistered()
	{
	}
}
