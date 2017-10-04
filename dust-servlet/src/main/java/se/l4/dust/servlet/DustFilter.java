package se.l4.dust.servlet;

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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Stage;

import se.l4.dust.api.Scopes;
import se.l4.dust.api.discovery.NamespaceDiscovery;
import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.servlet.internal.routing.FilterChainImpl;
import se.l4.dust.servlet.internal.routing.FilterEntry;
import se.l4.dust.servlet.internal.routing.ServletBinderImpl;
import se.l4.dust.servlet.internal.routing.ServletChain;
import se.l4.dust.servlet.internal.routing.ServletEntry;

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
		protected final Provider<RenderingContext> contexts;

		@Inject
		public Production(ServletBinderImpl binder, ServletContext ctx,
				Provider<RenderingContext> contexts)
		{
			this.binder = binder;
			this.ctx = ctx;
			this.contexts = contexts;
		}

		public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException
		{
			FilterChainImpl innerChain = new FilterChainImpl(
				binder.getFilters(),
				new ServletChain(
					binder.getServlets(),
					chain
				)
			);

			runChain(innerChain, request, response);
		}

		protected void runChain(FilterChain chain, ServletRequest request, ServletResponse response)
			throws IOException, ServletException
		{
			try
			{
				RenderingContext context = contexts.get();
				Scopes.setActiveContext(context);
				if(context instanceof WebRenderingContext)
				{
					((WebRenderingContext) context).setup((HttpServletRequest) request);
				}

				WebScopes.init((HttpServletRequest) request, (HttpServletResponse) response);

				chain.doFilter(request, response);
			}
			finally
			{
				WebScopes.clear();

				Scopes.clearActiveContext();
			}
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
		private final NamespaceDiscovery discovery;

		@Inject
		public Development(ServletBinderImpl binder, ServletContext ctx,
				Provider<RenderingContext> contexts,
				NamespaceDiscovery discovery)
		{
			super(binder, ctx, contexts);
			this.discovery = discovery;
		}

		@Override
		public void doFilter(final ServletRequest request, final ServletResponse response, FilterChain chain)
			throws IOException, ServletException
		{
			DevelopmentReloadChain reload = new DevelopmentReloadChain(discovery, chain);

			final FilterChainImpl innerChain = new FilterChainImpl(
				binder.getFilters(),
				new ServletChain(
					binder.getServlets(),
					reload
				)
			);

			reload.self = innerChain;

			runChain(innerChain, request, response);
		}
	}

	private static class DevelopmentReloadChain
		implements FilterChain
	{
		private final NamespaceDiscovery discovery;
		private final FilterChain chain;

		private FilterChain self;
		private boolean first;

		public DevelopmentReloadChain(NamespaceDiscovery discovery, FilterChain chain)
		{
			this.discovery = discovery;
			this.chain = chain;
			first = true;
		}

		@Override
		public void doFilter(ServletRequest request, ServletResponse response)
			throws IOException, ServletException
		{
			if(first)
			{
				first = false;

				/*
				 * If we are the first attempt and nothing can be found
				 * try to reindex all namespaces.
				 */
				discovery.performDiscovery();

				self.doFilter(request, response);
			}
			else
			{
				chain.doFilter(request, response);
			}
		}
	}

}
