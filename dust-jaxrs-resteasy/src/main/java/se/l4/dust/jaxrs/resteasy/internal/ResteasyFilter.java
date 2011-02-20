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

@Singleton
public class ResteasyFilter
	implements Filter
{
	private final ResteasyProviderFactory provider;

	@Inject
	public ResteasyFilter(ResteasyProviderFactory provider)
	{
		this.provider = provider;
	}
	
	public void init(FilterConfig filterConfig) throws ServletException
	{
		// TODO Auto-generated method stub
		
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException
	{
		provider.pushContext(HttpServletRequest.class, (HttpServletRequest) request);
		provider.pushContext(HttpServletResponse.class, (HttpServletResponse) response);
		
		chain.doFilter(request, response);
	}

	public void destroy()
	{
	}

}
