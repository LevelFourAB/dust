package se.l4.dust.core.internal.routing;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.google.common.collect.Iterators;

/**
 * Filter that should be applied to requests. Matches against a certain
 * pattern which the path of the request must match for this filter to be
 * applied.
 * 
 * @author Andreas Holstenson
 *
 */
public class FilterEntry
{
	private final String path;
	private final Matcher matcher;
	private final Filter filter;
	private final Map<String, String> params;

	public FilterEntry(String path, Matcher matcher, Filter filter, Map<String, String> params)
	{
		this.path = path;
		this.matcher = matcher;
		this.filter = filter;
		this.params = params;
	}
	
	public Filter getFilter()
	{
		return filter;
	}
	
	public void init(final ServletContext ctx)
		throws ServletException
	{
		filter.init(new FilterConfig()
		{
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
			
			public String getFilterName()
			{
				return path;
			}
		});
	}
	
	public void destroy()
	{
		filter.destroy();
	}
	
	public boolean matches(String path)
	{
		return matcher.matches(path);
	}
	
	public void doFilter(ServletRequest request, ServletResponse response, 
			FilterChain chain)
		throws IOException, ServletException
	{
		String path = ((HttpServletRequest) request).getPathInfo();
		
		if(matches(path))
		{
			filter.doFilter(request, response, chain);
		}
		else
		{
			chain.doFilter(request, response);
		}
	}
}
