package se.l4.dust.jaxrs;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

/**
 * Helper for creating response for static caching.
 * 
 * @author Andreas Holstenson 
 *
 */
public class CacheResponses
{
	private CacheResponses()
	{
	}
	
	public static ResponseBuilder longTermCacheResponse(Date lastModified)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
		Calendar c = Calendar.getInstance();
		c.add(Calendar.YEAR, 1);
		
		return Response.ok()
			.header("Cache-Control", "public, max-age=31536000000")
			.header("Expires", sdf.format(c.getTime()))
			.header("Last-Modified", sdf.format(lastModified));
	}
}
