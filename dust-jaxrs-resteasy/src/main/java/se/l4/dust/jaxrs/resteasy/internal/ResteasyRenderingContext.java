package se.l4.dust.jaxrs.resteasy.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Type;

import org.jboss.resteasy.spi.InjectorFactory;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import com.google.inject.Inject;
import com.google.inject.Stage;

import se.l4.dust.servlet.WebRenderingContext;

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
	public ResteasyRenderingContext(Stage stage, ResteasyProviderFactory factory)
	{
		super(stage);

		injectorFactory = factory.getInjectorFactory();
	}

	@Override
	public Object resolveObject(AccessibleObject parameter, Type type,
			Annotation[] annotations, Object instance)
	{
		return null;
	}
}
