package se.l4.dust.servlet.internal.routing;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class FilterChainImpl
	implements FilterChain
{
	private final FilterEntry[] entries;
	private int index;
	private final ServletChain fallback;

	public FilterChainImpl(FilterEntry[] entries, ServletChain fallback)
	{
		this.entries = entries;
		this.fallback = fallback;
		
		index = -1;
	}
	
	public void doFilter(ServletRequest request, ServletResponse response)
		throws IOException, ServletException
	{
		index++;
		
		if(index < entries.length)
		{
			FilterEntry entry = entries[index];
			entry.doFilter(request, response, this);
		}
		else
		{
			fallback.service(request, response);
		}
	}

}
