package se.l4.dust.jaxrs.resteasy;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.core.SynchronousDispatcher;
import org.jboss.resteasy.plugins.providers.ByteArrayProvider;
import org.jboss.resteasy.plugins.providers.DataSourceProvider;
import org.jboss.resteasy.plugins.providers.DefaultTextPlain;
import org.jboss.resteasy.plugins.providers.FileProvider;
import org.jboss.resteasy.plugins.providers.FormUrlEncodedProvider;
import org.jboss.resteasy.plugins.providers.IIOImageProvider;
import org.jboss.resteasy.plugins.providers.InputStreamProvider;
import org.jboss.resteasy.plugins.providers.ServerFormUrlEncodedProvider;
import org.jboss.resteasy.plugins.providers.StreamingOutputProvider;
import org.jboss.resteasy.plugins.providers.StringTextStar;
import org.jboss.resteasy.spi.Registry;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import se.l4.crayon.Contribution;
import se.l4.crayon.CrayonModule;
import se.l4.crayon.Order;
import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.jaxrs.JaxrsConfiguration;
import se.l4.dust.jaxrs.JaxrsModule;
import se.l4.dust.jaxrs.internal.template.TemplateWriter;
import se.l4.dust.jaxrs.resteasy.internal.ResteasyConfiguration;
import se.l4.dust.jaxrs.resteasy.internal.ResteasyFilter;
import se.l4.dust.jaxrs.resteasy.internal.ResteasyRenderingContext;
import se.l4.dust.servlet.ContextContribution;
import se.l4.dust.servlet.ServletBinder;

/**
 * Module for that activates Resteasy.
 *
 * @author Andreas Holstenson
 *
 */
public class ResteasyModule
	extends CrayonModule
{

	@Override
	protected void configure()
	{
		install(new JaxrsModule());

		// Bind a Resteasy factory
		ResteasyProviderFactory factory = new ResteasyProviderFactory();
		bind(ResteasyProviderFactory.class).toInstance(factory);
		ResteasyProviderFactory.setInstance(factory);

		// Bind the dispatcher
		bind(Dispatcher.class).toInstance(new SynchronousDispatcher(factory));

		// Bind SPI interfaces
		bind(JaxrsConfiguration.class).to(ResteasyConfiguration.class);
		bind(RenderingContext.class).to(ResteasyRenderingContext.class);
	}

	@Provides
	@Singleton
	public Registry provideRegistry(Dispatcher dispatcher)
	{
		return dispatcher.getRegistry();
	}

	@Provides
	public UriInfo provideUriInfo()
	{
		return ResteasyProviderFactory.getContextData(UriInfo.class);
	}

	@Provides
	public Request provideRequest()
	{
		return ResteasyProviderFactory.getContextData(Request.class);
	}

	@Contribution
	@Named("jax-rs-providers")
	public void contributeDefaultMessageProviders(ResteasyProviderFactory factory,
			ByteArrayProvider p1,
			DefaultTextPlain p2,
			FileProvider p3,
			FormUrlEncodedProvider p4,
			InputStreamProvider p5,
			StreamingOutputProvider p6,
			StringTextStar p7,
			IIOImageProvider p8,
			DataSourceProvider p9,
			TemplateWriter p10)
	{
		factory.registerProviderInstance(p1);
		factory.registerProviderInstance(p3);
		factory.registerProviderInstance(p4);
		factory.registerProviderInstance(p5);
		factory.registerProviderInstance(p6);
		factory.registerProviderInstance(p7);
		factory.registerProviderInstance(p8);
		factory.registerProviderInstance(p9);
		factory.registerProviderInstance(p2);
		factory.registerProviderInstance(p10);
		factory.registerProviderInstance(new ServerFormUrlEncodedProvider(false));
	}

	@ContextContribution
	@Order("last")
	public void setupResteasy(ServletContext ctx,
			ResteasyProviderFactory factory,
			Dispatcher dispatcher,
			Registry registry,
			ServletBinder binder)
	{
		// Register the services in the context
		ctx.setAttribute(ResteasyProviderFactory.class.getName(), factory);
		ctx.setAttribute(Dispatcher.class.getName(), dispatcher);
		ctx.setAttribute(Registry.class.getName(), registry);

		// And bind a new filter
		binder.filter("/*").with(ResteasyFilter.class);
	}
}
