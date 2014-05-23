package se.l4.dust.jaxrs.resteasy.internal;

import javax.servlet.ServletContext;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.ParamConverterProvider;

import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.spi.Registry;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import se.l4.dust.jaxrs.PageProvider;
import se.l4.dust.jaxrs.ServletBinder;
import se.l4.dust.jaxrs.spi.Configuration;

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
	implements Configuration
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
	
	@Override
	public void setupContext(ServletContext ctx, Injector injector)
	{
		this.servletContext = ctx;
		
		// Setup Resteasy
		ResteasyProviderFactory factory = injector.getInstance(ResteasyProviderFactory.class);
		Dispatcher dispatcher = injector.getInstance(Dispatcher.class);
		Registry registry = injector.getInstance(Registry.class);
		
		// Register the services in the context
		ctx.setAttribute(ResteasyProviderFactory.class.getName(), factory);
		ctx.setAttribute(Dispatcher.class.getName(), dispatcher);
		ctx.setAttribute(Registry.class.getName(), registry);
	}
	
	@Override
	public void setupFilter(ServletContext ctx, Injector injector,
			ServletBinder binder)
	{
		binder.filter("/*").with(ResteasyFilter.class);
	}

	public ServletContext getServletContext()
	{
		return servletContext;
	}
}
