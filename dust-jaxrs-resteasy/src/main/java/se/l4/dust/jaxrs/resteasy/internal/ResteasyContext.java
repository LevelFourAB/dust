package se.l4.dust.jaxrs.resteasy.internal;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jboss.resteasy.spi.ResteasyProviderFactory;

import se.l4.dust.jaxrs.spi.Context;

public class ResteasyContext
	implements Context
{
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
		return ResteasyProviderFactory.getContextData(ServletContext.class);
	}

}
