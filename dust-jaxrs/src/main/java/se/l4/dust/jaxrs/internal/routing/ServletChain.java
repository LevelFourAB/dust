package se.l4.dust.jaxrs.internal.routing;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class ServletChain
{
	private final ServletEntry[] entries;
	private final FilterChain fallback;
	
	private int index;

	public ServletChain(ServletEntry[] entries, FilterChain fallback)
	{
		this.entries = entries;
		this.fallback = fallback;
		
		index = -1;
	}
	
	public void service(ServletRequest request, ServletResponse response)
		throws IOException, ServletException
	{
		index++;
		
		if(index < entries.length)
		{
			ServletEntry entry = entries[index];
			entry.service(request, response, this);
		}
		else
		{
			fallback.doFilter(request, response);
		}
	}
}
