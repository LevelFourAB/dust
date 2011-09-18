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

import se.l4.dust.jaxrs.internal.ServletBinderImpl;
import se.l4.dust.jaxrs.internal.routing.FilterChainImpl;
import se.l4.dust.jaxrs.internal.routing.FilterEntry;
import se.l4.dust.jaxrs.internal.routing.ServletChain;
import se.l4.dust.jaxrs.internal.routing.ServletEntry;
import se.l4.dust.jaxrs.spi.Configuration;
import se.l4.dust.jaxrs.spi.Context;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Stage;

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
	private Production impl;
	
	public DustFilter()
	{
	}
	
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain)
		throws IOException, ServletException
	{
		impl.doFilter(request, response, chain);
	}
	
	public void init(FilterConfig filterConfig)
		throws ServletException
	{
		// Get our needed services
		ServletContext ctx = filterConfig.getServletContext();
		Injector injector = (Injector) ctx.getAttribute(Injector.class.getName());
		
		// Setup context for scoping
		WebScopes.setContext(injector.getInstance(Context.class));
		
		// Setup the configuration filter
		Configuration config = injector.getInstance(Configuration.class);
		ServletBinder binder = injector.getInstance(ServletBinderImpl.class);
		config.setupFilter(ctx, injector, binder);
		
		// Create the real implementation
		Stage stage = injector.getInstance(Stage.class);
		impl = injector.getInstance(stage == Stage.PRODUCTION ? Production.class : Development.class);
		
		// Initialize all filters and servlets
		impl.doInit();
	}

	public void destroy()
	{
		impl.destroy();
	}
	
	private static class Production
	{
		protected final ServletBinderImpl binder;
		protected final ServletContext ctx;
		
		@Inject
		public Production(ServletBinderImpl binder, ServletContext ctx)
		{
			this.binder = binder;
			this.ctx = ctx;
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
		
		public void doInit()
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
	}
	
	private static class Development
		extends Production
	{
		@Inject
		public Development(ServletBinderImpl binder, ServletContext ctx)
		{
			super(binder, ctx);
		}
	}
	
}
