package se.l4.dust.jaxrs.internal.routing;

import java.io.IOException;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class FilterChainImpl
	implements FilterChain
{
	private final List<FilterEntry> entries;
	private int index;
	private final ServletChain fallback;

	public FilterChainImpl(List<FilterEntry> entries, ServletChain fallback)
	{
		this.entries = entries;
		this.fallback = fallback;
		
		index = -1;
	}
	
	public void doFilter(ServletRequest request, ServletResponse response)
		throws IOException, ServletException
	{
		index++;
		
		if(index < entries.size())
		{
			FilterEntry entry = entries.get(index);
			entry.doFilter(request, response, this);
		}
		else
		{
			fallback.service(request, response);
		}
	}

}
