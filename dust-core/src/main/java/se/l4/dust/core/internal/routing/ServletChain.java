package se.l4.dust.core.internal.routing;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class ServletChain
{
	private final List<ServletEntry> entries;
	private int index;

	public ServletChain(List<ServletEntry> entries)
	{
		this.entries = entries;
		
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
		
		// The last entry should be our own WebServlet 
	}
}
