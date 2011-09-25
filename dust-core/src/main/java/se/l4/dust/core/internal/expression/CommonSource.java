package se.l4.dust.core.internal.expression;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import se.l4.dust.api.expression.ReflectiveExpressionSource;
import se.l4.dust.api.template.dom.Element;

/**
 * Source of common properties and methods used within expressions.
 * 
 * @author Andreas Holstenson
 *
 */
public class CommonSource
	extends ReflectiveExpressionSource
{
	@Property
	public Object skip()
	{
		return Element.Attribute.ATTR_SKIP;
	}
	
	@Property
	public Object emit()
	{
		return Element.Attribute.ATTR_EMIT;
	}
	
	@Method
	public String urlencode(String in)
	{
		try
		{
			return URLEncoder.encode(in, "UTF-8");
		}
		catch(UnsupportedEncodingException e)
		{
			throw new AssertionError("UTF-8 unsupported");
		}
	}
}
