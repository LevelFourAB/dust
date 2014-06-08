package se.l4.dust.jaxrs.resteasy.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Type;

import org.jboss.resteasy.spi.InjectorFactory;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import se.l4.dust.servlet.WebRenderingContext;

import com.google.inject.Inject;

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
	public ResteasyRenderingContext(ResteasyProviderFactory factory)
	{
		injectorFactory = factory.getInjectorFactory();
	}

	@Override
	public Object resolveObject(AccessibleObject parameter, Type type, 
			Annotation[] annotations, Object instance)
	{
		return null;
	}
}
