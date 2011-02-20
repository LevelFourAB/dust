package se.l4.dust.jaxrs;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import se.l4.crayon.CrayonModule;
import se.l4.crayon.annotation.Contribution;
import se.l4.dust.api.asset.AssetManager;
import se.l4.dust.core.CoreModule;
import se.l4.dust.jaxrs.annotation.ContextScoped;
import se.l4.dust.jaxrs.annotation.RequestScoped;
import se.l4.dust.jaxrs.annotation.SessionScoped;
import se.l4.dust.jaxrs.internal.NormalPageManager;
import se.l4.dust.jaxrs.internal.ServletBinderImpl;
import se.l4.dust.jaxrs.internal.asset.AssetProvider;
import se.l4.dust.jaxrs.internal.asset.AssetWriter;
import se.l4.dust.jaxrs.internal.asset.ContextAssetSource;
import se.l4.dust.jaxrs.internal.template.TemplateWriter;
import se.l4.dust.jaxrs.spi.Configuration;
import se.l4.dust.jaxrs.spi.Context;

import com.google.inject.Provides;

/**
 * Module defining shared web bindings.
 * 
 * @author andreas
 *
 */
public class WebModule
	extends CrayonModule
{
	@Override
	protected void configure()
	{
		install(new CoreModule());
		
		// Bind scopes
		bindScope(SessionScoped.class, WebScopes.SESSION);
		bindScope(RequestScoped.class, WebScopes.REQUEST);
		bindScope(ContextScoped.class, WebScopes.CONTEXT);
		
		// Bind own services
		bind(PageManager.class).to(NormalPageManager.class);
		bind(ServletBinder.class).to(ServletBinderImpl.class);
	}
	
	@Contribution(name="asset-page")
	public void contributeAssetPage(PageManager manager)
	{
		manager.add(AssetProvider.class);
	}
	
	@Contribution(name="asset-sources")
	public void contributeClasspathSource(AssetManager manager)
	{
		manager.addSource(ContextAssetSource.class);
	}
	
	@Contribution(name="message-provider")
	public void contributeDefaultMessageProviders(Configuration config,
			AssetWriter w1,
			TemplateWriter w2)
	{
		config.addMessageBodyWriter(w1);
		config.addMessageBodyWriter(w2);
	}
	
	@Provides
	public HttpServletRequest provideHttpServletRequest(Context ctx)
	{
		return ctx.getHttpServletRequest();
	}
	
	@Provides
	public HttpServletResponse provideHttpServletResponse(Context ctx)
	{
		return ctx.getHttpServletResponse();
	}
	
	@Provides
	public HttpSession provideHttpSession(Context ctx)
	{
		return ctx.getHttpSession();
	}
	
	@Provides
	public ServletContext provideServletContext(Context ctx)
	{
		return ctx.getServletContext();
	}
}
