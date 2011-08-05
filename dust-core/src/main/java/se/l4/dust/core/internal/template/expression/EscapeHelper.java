package se.l4.dust.core.internal.template.expression;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import se.l4.dust.api.template.dom.Element;


/**
 * Helper class with methods for escaping of template variables.
 * 
 * @author Andreas Holstenson
 *
 */
public class EscapeHelper
{
	public static final String skip = Element.Attribute.ATTR_SKIP;
	public static final String emit = Element.Attribute.ATTR_EMIT;
	
	private EscapeHelper()
	{
	}
	
	public static String url(String in) 
		throws UnsupportedEncodingException
	{
		return URLEncoder.encode(in, "UTF-8").replace("+", "%20");
	}
	
	/**
	 * Special string value for skipping output of the given attribute.
	 * 
	 * @return
	 */
	public static String skip(boolean skip)
	{
		return skip ? Element.Attribute.ATTR_SKIP : Element.Attribute.ATTR_EMIT;
	}
	
	/**
	 * Special string value for emitting the given attribute without a value.
	 * 
	 * @return
	 */
	public static String emit(boolean emit)
	{
		return emit ? Element.Attribute.ATTR_EMIT : Element.Attribute.ATTR_SKIP;
	}
}
