package se.l4.dust.jaxrs.resteasy.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Type;

import org.jboss.resteasy.core.ValueInjector;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.resteasy.spi.InjectorFactory;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.resteasy.util.Types;

import com.google.inject.Inject;

import se.l4.dust.api.NamespaceManager;
import se.l4.dust.api.conversion.TypeConverter;
import se.l4.dust.jaxrs.spi.WebRenderingContext;

/**
 * Extension to {@link WebRenderingContext} for Resteasy specific 
 * injections.
 * 
 * @author Andreas Holstenson
 *
 */
public class ResteasyRenderingContext
	extends WebRenderingContext
{
	private final InjectorFactory injectorFactory;

	@Inject
	public ResteasyRenderingContext(NamespaceManager namespaceManager, TypeConverter converter, ResteasyProviderFactory factory)
	{
		super(namespaceManager, converter);
		injectorFactory = factory.getInjectorFactory();
	}

	@Override
	public Object resolveObject(AccessibleObject parameter, Type type, 
			Annotation[] annotations, Object instance)
	{
		return null;
	}
}
