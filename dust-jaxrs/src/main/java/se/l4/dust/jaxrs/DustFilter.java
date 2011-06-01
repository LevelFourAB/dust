package se.l4.dust.jaxrs;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.google.inject.Injector;

import se.l4.dust.jaxrs.internal.ServletBinderImpl;
import se.l4.dust.jaxrs.internal.routing.FilterChainImpl;
import se.l4.dust.jaxrs.internal.routing.FilterEntry;
import se.l4.dust.jaxrs.internal.routing.ServletChain;
import se.l4.dust.jaxrs.internal.routing.ServletEntry;
import se.l4.dust.jaxrs.spi.Configuration;
import se.l4.dust.jaxrs.spi.Context;

/**
 * Filter that performs serving via the registered filters and servlets in
 * {@link ServletBinder}. This is used as a way to define filters and servlets
 * in code and allow them to be injected via Guice. This filter is not required
 * for the use of Dust and should not be included if you intend to declare
 * filters and servlets in your web.xml.
 * 
 * @author Andreas Holstenson
 *
 */
public class DustFilter
	implements Filter
{
//	private ResteasyProviderFactory provider;
	private ServletBinderImpl binder;
	private ServletContext ctx;

	public DustFilter()
	{
	}
	
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain)
		throws IOException, ServletException
	{
		// Perform filtering
		FilterChainImpl innerChain = new FilterChainImpl(
			binder.getFilters(),
			new ServletChain(
				binder.getServlets(),
				chain
			)
		);
		
		innerChain.doFilter(request, response);
	}
	
	private void doInit()
		throws ServletException
	{
		Set<Object> inited = new HashSet<Object>();
		
		for(FilterEntry e : binder.getFilters())
		{
			if(inited.add(e.getFilter()))
			{
				e.init(ctx);
			}
		}
		
		for(ServletEntry e : binder.getServlets())
		{
			if(inited.add(e.getServlet()))
			{
				e.init(ctx);
			}
		}
	}

	public void init(FilterConfig filterConfig)
		throws ServletException
	{
		// Get our needed services
		ctx = filterConfig.getServletContext();
		Injector injector = (Injector) ctx.getAttribute(Injector.class.getName());
		binder = injector.getInstance(ServletBinderImpl.class);
		
		// Setup context for scoping
		WebScopes.setContext(injector.getInstance(Context.class));
		
		Configuration config = injector.getInstance(Configuration.class);
		config.setupFilter(ctx, injector, binder);
		
		// Initialize all filters and servlets
		doInit();
	}

	public void destroy()
	{
		Set<Object> destroyed = new HashSet<Object>();
		
		for(FilterEntry e : binder.getFilters())
		{
			if(destroyed.add(e.getFilter()))
			{
				e.destroy();
			}
		}
		
		for(ServletEntry e : binder.getServlets())
		{
			if(destroyed.add(e.getServlet()))
			{
				e.destroy();
			}
		}
	}
}
