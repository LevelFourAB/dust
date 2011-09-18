package se.l4.dust.jaxrs.resteasy.internal;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;

import org.jboss.resteasy.core.SynchronousDispatcher;
import org.jboss.resteasy.plugins.server.servlet.FilterBootstrap;
import org.jboss.resteasy.plugins.server.servlet.HttpRequestFactory;
import org.jboss.resteasy.plugins.server.servlet.HttpResponseFactory;
import org.jboss.resteasy.plugins.server.servlet.HttpServletInputMessage;
import org.jboss.resteasy.plugins.server.servlet.HttpServletResponseWrapper;
import org.jboss.resteasy.plugins.server.servlet.ServletContainerDispatcher;
import org.jboss.resteasy.specimpl.UriInfoImpl;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.resteasy.spi.NotFoundException;

import com.google.inject.Singleton;

/**
 * Filter that specifically works with Resteasy. This filter will push the
 * proper objects onto the Resteasy stack.
 * 
 * @author Andreas Holstenson
 *
 */
@Singleton
public class ResteasyFilter
	implements Filter, HttpRequestFactory, HttpResponseFactory
{
	private ServletContainerDispatcher servletContainerDispatcher;
	
	public ResteasyFilter()
	{
	}
	
	public void init(FilterConfig filterConfig)
		throws ServletException
	{
		servletContainerDispatcher = new ServletContainerDispatcher();
		FilterBootstrap bootstrap = new FilterBootstrap(filterConfig);
		servletContainerDispatcher.init(filterConfig.getServletContext(), bootstrap, this, this);
		servletContainerDispatcher.getDispatcher().getDefaultContextObjects().put(FilterConfig.class, filterConfig);
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
		throws IOException, ServletException
	{
		try
		{
			servletContainerDispatcher.service(
				((HttpServletRequest) request).getMethod(),
				(HttpServletRequest) request,
				(HttpServletResponse) response, 
				false
			);
		}
		catch(NotFoundException e)
		{
			chain.doFilter(request, response);
		}
	}

	public void destroy()
	{
		servletContainerDispatcher.destroy();
	}

	@Override
	public HttpRequest createResteasyHttpRequest(String httpMethod,
			HttpServletRequest request, HttpHeaders headers,
			UriInfoImpl uriInfo, HttpResponse theResponse,
			HttpServletResponse response)
	{
		return new HttpServletInputMessage(request, theResponse, headers,
			uriInfo, httpMethod.toUpperCase(),
			dispatcher()
		);
	}

	private SynchronousDispatcher dispatcher()
	{
		return (SynchronousDispatcher) servletContainerDispatcher.getDispatcher();
	}

	@Override
	public HttpResponse createResteasyHttpResponse(HttpServletResponse response)
	{
		return new HttpServletResponseWrapper(response, dispatcher().getProviderFactory());
	}
}
