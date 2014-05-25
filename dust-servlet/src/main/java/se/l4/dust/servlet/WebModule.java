package se.l4.dust.servlet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import se.l4.crayon.Contributions;
import se.l4.crayon.CrayonModule;
import se.l4.crayon.annotation.Contribution;
import se.l4.crayon.annotation.Order;
import se.l4.dust.api.resource.Resources;
import se.l4.dust.core.CoreModule;
import se.l4.dust.servlet.internal.ContextResourceLocator;
import se.l4.dust.servlet.internal.routing.ServletBinderImpl;

import com.google.inject.Provides;

/**
 * Module defining shared web bindings.
 * 
 * @author Andreas Holstenson
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
		
		// Bind own services
		bind(ServletBinder.class).to(ServletBinderImpl.class);
		
		// Bind up the filter annotations
		bindContributions(FilterOrServletContribution.class);
		bindContributions(ContextContribution.class);
	}
	
	@Contribution(name="dust-context-resource-locator")
	@Order("before:dust-assets")
	public void contributeContextLocator(Resources resources, ContextResourceLocator locator)
	{
		resources.addLocator(locator);
	}
	
	@Contribution(name="dust-filter-contributions")
	public void contributeFilters(@FilterOrServletContribution Contributions contributions)
	{
		contributions.run();
	}
	
	@Provides
	public HttpServletRequest provideHttpServletRequest()
	{
		return WebScopes.getRequest();
	}
	
	@Provides
	public HttpServletResponse provideHttpServletResponse()
	{
		return WebScopes.getResponse();
	}
	
	@Provides
	public HttpSession provideHttpSession()
	{
		return WebScopes.getRequest().getSession(true);
	}
	
	@Provides
	public ServletContext provideServletContext()
	{
		return WebScopes.getContext();
	}
}
