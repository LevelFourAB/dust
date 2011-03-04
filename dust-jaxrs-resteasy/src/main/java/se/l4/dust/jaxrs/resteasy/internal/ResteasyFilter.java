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

import org.jboss.resteasy.spi.ResteasyProviderFactory;

import com.google.inject.Inject;
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
	implements Filter
{
	@Inject
	public ResteasyFilter()
	{
	}
	
	public void init(FilterConfig filterConfig)
		throws ServletException
	{
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
		throws IOException, ServletException
	{
		ResteasyProviderFactory.pushContext(HttpServletRequest.class, (HttpServletRequest) request);
		ResteasyProviderFactory.pushContext(HttpServletResponse.class, (HttpServletResponse) response);
		
		chain.doFilter(request, response);
		
		ResteasyProviderFactory.popContextData(HttpServletResponse.class);
		ResteasyProviderFactory.popContextData(HttpServletRequest.class);
	}

	public void destroy()
	{
	}

}
