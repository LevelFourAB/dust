package se.l4.dust.jaxrs.resteasy;

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
import se.l4.dust.jaxrs.spi.Context;

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
		bind(Context.class).to(ResteasyContext.class);
		bind(RenderingContext.class).to(ResteasyRenderingContext.class);
	}
	
	@Provides
	@Singleton
	public Registry provideRegistry(Dispatcher dispatcher)
	{
		return dispatcher.getRegistry();
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
		factory.addMessageBodyReader(p1);
		factory.addMessageBodyWriter(p1);
		
		factory.addMessageBodyReader(p3);
		factory.addMessageBodyWriter(p3);
		
		factory.addMessageBodyReader(p4);
		factory.addMessageBodyWriter(p4);
		
		factory.addMessageBodyReader(p5);
		factory.addMessageBodyWriter(p5);
		
		factory.addMessageBodyWriter(p6);
		
		factory.addMessageBodyReader(p7);
		factory.addMessageBodyWriter(p7);
		
		factory.addMessageBodyReader(p8);
		factory.addMessageBodyWriter(p8);
		
		factory.addMessageBodyReader(p9);
		factory.addMessageBodyWriter(p9);
		
		factory.addMessageBodyReader(p2);
		factory.addMessageBodyWriter(p2);
		
		factory.addMessageBodyWriter(p10);
	}
}
