package se.l4.dust.core;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.core.SynchronousDispatcher;
import org.jboss.resteasy.plugins.providers.ByteArrayProvider;
import org.jboss.resteasy.plugins.providers.DataSourceProvider;
import org.jboss.resteasy.plugins.providers.DefaultTextPlain;
import org.jboss.resteasy.plugins.providers.FileProvider;
import org.jboss.resteasy.plugins.providers.FormUrlEncodedProvider;
import org.jboss.resteasy.plugins.providers.IIOImageProvider;
import org.jboss.resteasy.plugins.providers.InputStreamProvider;
import org.jboss.resteasy.plugins.providers.StreamingOutputProvider;
import org.jboss.resteasy.plugins.providers.StringTextStar;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.resteasy.spi.Registry;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import com.google.inject.Binder;
import com.google.inject.Provider;

import se.l4.crayon.annotation.Contribution;
import se.l4.crayon.annotation.Dependencies;
import se.l4.crayon.annotation.Description;
import se.l4.dust.api.NamespaceManager;
import se.l4.dust.api.PageProviderManager;
import se.l4.dust.api.WebScopes;
import se.l4.dust.api.annotation.RequestScoped;
import se.l4.dust.api.annotation.SessionScoped;
import se.l4.dust.core.internal.NamespaceManagerImpl;
import se.l4.dust.core.internal.PageProviderManagerImpl;
import se.l4.dust.core.internal.asset.AssetModule;
import se.l4.dust.core.template.TemplateModule;
import se.l4.dust.core.template.TemplateWriter;

@Dependencies({ TemplateModule.class, AssetModule.class })
public class WebModule
{
	@Description(name="web")
	public void configure(Binder binder)
	{
		// Bind scopes
		binder.bindScope(SessionScoped.class, WebScopes.SESSION);
		binder.bindScope(RequestScoped.class, WebScopes.REQUEST);
		
		// Bind own services
		binder.bind(PageProviderManager.class).to(PageProviderManagerImpl.class);
		binder.bind(NamespaceManager.class).to(NamespaceManagerImpl.class);
		
		// Bind servlet interfaces
		binder.bind(HttpServletRequest.class).toProvider(
			new Provider<HttpServletRequest>()
			{
				public HttpServletRequest get()
				{
					return ResteasyProviderFactory.getContextData(HttpServletRequest.class);
				}
				
				@Override
				public String toString()
				{
					return "HttpServletRequest";
				}
			});
		
		binder.bind(HttpServletResponse.class).toProvider(
			new Provider<HttpServletResponse>()
			{
				public HttpServletResponse get()
				{
					return ResteasyProviderFactory.getContextData(HttpServletResponse.class);
				}
				
				@Override
				public String toString()
				{
					return "HttpServletResponse";
				}
			});
		
		binder.bind(HttpSession.class).toProvider(
			new Provider<HttpSession>()
			{
				public HttpSession get()
				{
					return ResteasyProviderFactory.getContextData(HttpServletRequest.class)
						.getSession();
				}
				
				@Override
				public String toString()
				{
					return "HttpSession";
				}
			});
		
		binder.bind(ServletContext.class).toProvider(
			new Provider<ServletContext>()
			{
				public ServletContext get()
				{
					return ResteasyProviderFactory.getContextData(ServletContext.class);
				}
				
				@Override
				public String toString()
				{
					return "ServletContext";
				}
			});
		
		// Bind Resteasy SPI interfaces
		binder.bind(HttpRequest.class).toProvider(
			new Provider<HttpRequest>()
			{
				public HttpRequest get()
				{
					return ResteasyProviderFactory.getContextData(HttpRequest.class);
				}
				
				@Override
				public String toString()
				{
					return "HttpRequest";
				}
			});
		
		binder.bind(HttpResponse.class).toProvider(
			new Provider<HttpResponse>()
			{
				public HttpResponse get()
				{
					return ResteasyProviderFactory.getContextData(HttpResponse.class);
				}
				
				@Override
				public String toString()
				{
					return "HttpResponse";
				}
			});
		
		// Bind a Resteasy factory
		ResteasyProviderFactory factory = new ResteasyProviderFactory();
		binder.bind(ResteasyProviderFactory.class).toInstance(factory);
		ResteasyProviderFactory.setInstance(factory);
		
		// Bind a dispatcher
		Dispatcher dispatcher = new SynchronousDispatcher(factory);
		binder.bind(Dispatcher.class).toInstance(dispatcher);
		
		// Bind the registry
		Registry registry = dispatcher.getRegistry();
		binder.bind(Registry.class).toInstance(registry);		
		
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
