package se.l4.dust.jaxrs.resteasy.internal;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.spi.Registry;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import se.l4.dust.jaxrs.PageProvider;
import se.l4.dust.jaxrs.ServletBinder;
import se.l4.dust.jaxrs.spi.Configuration;

import com.google.inject.Inject;
import com.google.inject.Injector;


public class ResteasyConfiguration
	implements Configuration
{
	private final Registry registry;
	private final ResteasyProviderFactory factory;
	
	@Inject
	public ResteasyConfiguration(Registry registry,
			ResteasyProviderFactory factory)
	{
		this.registry = registry;
		this.factory = factory;
	}
	
	public void addPage(PageProvider factory)
	{
		registry.addResourceFactory(new PageResourceFactory(factory));
	}

	public void removePage(PageProvider factory)
	{
		// TODO: IMPLEMENT
	}

	public void addMessageBodyReader(MessageBodyReader<?> reader)
	{
		factory.addMessageBodyReader(reader);
	}
	
	public void addMessageBodyWriter(MessageBodyWriter<?> writer)
	{
		factory.addMessageBodyWriter(writer);
	}
	
	public void setupContext(ServletContext ctx, Injector injector)
	{
		// Setup Resteasy
		ResteasyProviderFactory factory = injector.getInstance(ResteasyProviderFactory.class);
		Dispatcher dispatcher = injector.getInstance(Dispatcher.class);
		Registry registry = injector.getInstance(Registry.class);
		
		// Register the services in the context
		ctx.setAttribute(ResteasyProviderFactory.class.getName(), factory);
		ctx.setAttribute(Dispatcher.class.getName(), dispatcher);
		ctx.setAttribute(Registry.class.getName(), registry);
	}
	
	public Class<? extends HttpServlet> getRootServlet()
	{
		return WebServlet.class;
	}
	
	public void setupFilter(ServletContext ctx, Injector injector,
			ServletBinder binder)
	{
		binder.filter("/*").with(ResteasyFilter.class);
	}
}
