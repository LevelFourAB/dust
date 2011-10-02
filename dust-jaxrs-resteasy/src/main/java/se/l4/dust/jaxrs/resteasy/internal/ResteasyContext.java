package se.l4.dust.jaxrs.resteasy.internal;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jboss.resteasy.spi.ResteasyProviderFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import se.l4.dust.jaxrs.spi.RequestContext;

/**
 * Context for Reaseasy, mostly delegates to {@link ResteasyProviderFactory}.
 * 
 * @author Andreas Holstenson
 *
 */
@Singleton
public class ResteasyContext
	implements RequestContext
{
	private final ResteasyConfiguration configuration;

	@Inject
	public ResteasyContext(ResteasyConfiguration configuration)
	{
		this.configuration = configuration;
	}
	
	public HttpServletRequest getHttpServletRequest()
	{
		return ResteasyProviderFactory.getContextData(HttpServletRequest.class);
	}

	public HttpServletResponse getHttpServletResponse()
	{
		return ResteasyProviderFactory.getContextData(HttpServletResponse.class);
	}

	public HttpSession getHttpSession()
	{
		return ResteasyProviderFactory.getContextData(HttpServletRequest.class)
			.getSession();
	}

	public ServletContext getServletContext()
	{
		return configuration.getServletContext();
	}

}
