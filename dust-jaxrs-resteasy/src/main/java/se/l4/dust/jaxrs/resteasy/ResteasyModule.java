package se.l4.dust.jaxrs.resteasy;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.plugins.providers.ByteArrayProvider;
import org.jboss.resteasy.plugins.providers.DataSourceProvider;
import org.jboss.resteasy.plugins.providers.DefaultTextPlain;
import org.jboss.resteasy.plugins.providers.FileProvider;
import org.jboss.resteasy.plugins.providers.FormUrlEncodedProvider;
import org.jboss.resteasy.plugins.providers.IIOImageProvider;
import org.jboss.resteasy.plugins.providers.InputStreamProvider;
import org.jboss.resteasy.plugins.providers.StreamingOutputProvider;
import org.jboss.resteasy.plugins.providers.StringTextStar;
import org.jboss.resteasy.spi.Registry;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import se.l4.crayon.CrayonModule;
import se.l4.crayon.annotation.Contribution;
import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.jaxrs.WebModule;
import se.l4.dust.jaxrs.internal.template.TemplateWriter;
import se.l4.dust.jaxrs.resteasy.internal.ReloadingDispatcher;
import se.l4.dust.jaxrs.resteasy.internal.ResteasyConfiguration;
import se.l4.dust.jaxrs.resteasy.internal.ResteasyContext;
import se.l4.dust.jaxrs.resteasy.internal.ResteasyRenderingContext;
import se.l4.dust.jaxrs.spi.Configuration;
import se.l4.dust.jaxrs.spi.RequestContext;

import com.google.inject.Provides;
import com.google.inject.Singleton;

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
		install(new WebModule());
		
		// Bind a Resteasy factory
		ResteasyProviderFactory factory = new ResteasyProviderFactory();
		bind(ResteasyProviderFactory.class).toInstance(factory);
		ResteasyProviderFactory.setInstance(factory);
		
		// Bind a dispatcher
		bind(Dispatcher.class).to(ReloadingDispatcher.class);
		
		// Bind SPI interfaces 
		bind(Configuration.class).to(ResteasyConfiguration.class);
		bind(RequestContext.class).to(ResteasyContext.class);
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
	
	@Contribution(name="jax-rs-providers")
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
	}
}
