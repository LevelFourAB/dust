package se.l4.dust.core.template;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


/**
 * Helper class with methods for escaping of template variables.
 * 
 * @author Andreas Holstenson
 *
 */
public class EscapeHelper
{
	private EscapeHelper()
	{
	}
	
	public static String url(String in) 
		throws UnsupportedEncodingException
	{
		return URLEncoder.encode(in, "UTF-8").replace("+", "%20");
	}
}
