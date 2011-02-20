package se.l4.dust.jaxrs.internal.routing;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.google.common.collect.Iterators;

public class ServletEntry
{
	private final String path;
	private final Matcher matcher;
	private final Servlet servlet;
	private final Map<String, String> params;

	public ServletEntry(String path, Matcher matcher, Servlet servlet, Map<String, String> params)
	{
		this.path = path;
		this.matcher = matcher;
		this.servlet = servlet;
		this.params = params;
	}
	
	public Servlet getServlet()
	{
		return servlet;
	}
	
	public void init(final ServletContext ctx)
		throws ServletException
	{
		servlet.init(new ServletConfig()
		{
			public String getServletName()
			{
				return path;
			}
			
			public ServletContext getServletContext()
			{
				return ctx;
			}
			
			public Enumeration getInitParameterNames()
			{
				return Iterators.asEnumeration(params.keySet().iterator());
			}
			
			public String getInitParameter(String name)
			{
				return params.get(name);
			}
		});
	}
	
	public void destroy()
	{
		servlet.destroy();
	}
	
	public boolean matches(String path)
	{
		return matcher.matches(path);
	}
	
	public void service(ServletRequest request, ServletResponse response, ServletChain chain)
		throws IOException, ServletException
	{
		String path = ((HttpServletRequest) request).getPathInfo();
		if(path == null)
		{
			path = ((HttpServletRequest) request).getServletPath();
		}
		
		if(matches(path))
		{
			servlet.service(request, response);
		}
		else
		{
			chain.service(request, response);
		}
	}
}
