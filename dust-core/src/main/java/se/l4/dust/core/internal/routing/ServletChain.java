package se.l4.dust.core.internal.routing;

import java.io.IOException;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class ServletChain
{
	private final List<ServletEntry> entries;
	private final FilterChain fallback;
	
	private int index;

	public ServletChain(List<ServletEntry> entries, FilterChain fallback)
	{
		this.entries = entries;
		this.fallback = fallback;
		
		index = -1;
	}
	
	public void service(ServletRequest request, ServletResponse response)
		throws IOException, ServletException
	{
		index++;
		
		if(index < entries.size())
		{
			ServletEntry entry = entries.get(index);
			entry.service(request, response, this);
		}
		else
		{
			fallback.doFilter(request, response);
		}
	}
}
