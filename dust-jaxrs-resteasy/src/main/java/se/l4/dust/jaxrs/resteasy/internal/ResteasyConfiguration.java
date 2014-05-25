package se.l4.dust.jaxrs.resteasy.internal;

import javax.servlet.ServletContext;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.ParamConverterProvider;

import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.spi.Registry;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import se.l4.dust.jaxrs.JaxrsConfiguration;
import se.l4.dust.servlet.ServletBinder;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.Stage;

/**
 * Configuration for Resteasy.
 * 
 * @author Andreas Holstenson
 *
 */
@Singleton
public class ResteasyConfiguration
	implements JaxrsConfiguration
{
	private final Injector injector;
	private final Registry registry;
	private final ResteasyProviderFactory factory;
	private final boolean dev;
	
	private ServletContext servletContext;
	
	@Inject
	public ResteasyConfiguration(
			Injector injector,
			Stage stage,
			Registry registry,
			ResteasyProviderFactory factory)
	{
		this.injector = injector;
		this.registry = registry;
		this.factory = factory;
		
		dev = stage == Stage.DEVELOPMENT;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addPage(final Class<?> typeAnnotatedWithPath)
	{
		Provider<Object> provider;
		if(dev)
		{
			provider = new Provider<Object>()
			{
				@Override
				public Object get()
				{
					return injector.getInstance(typeAnnotatedWithPath);
				}
			};
		}
		else
		{
			provider = (Provider<Object>) injector.getProvider(typeAnnotatedWithPath);
		}
		
		registry.addResourceFactory(new PageResourceFactory(provider, typeAnnotatedWithPath));
	}
	@Override
	public void addMessageBodyReader(MessageBodyReader<?> reader)
	{
		factory.register(reader);
	}
	
	@Override
	public void addMessageBodyWriter(MessageBodyWriter<?> writer)
	{
		factory.register(writer);
	}
	
	@Override
	public void addExceptionMapper(ExceptionMapper<?> mapper)
	{
		factory.register(mapper);
	}
	
	@Override
	public void addParamConverterProvider(ParamConverterProvider provider)
	{
		factory.register(provider);
	}
	
	public ServletContext getServletContext()
	{
		return servletContext;
	}
}
