package se.l4.dust.core.internal.expression;

import se.l4.dust.api.expression.ReflectiveExpressionSource;
import se.l4.dust.api.template.dom.Attribute;

import com.google.common.escape.Escaper;
import com.google.common.html.HtmlEscapers;
import com.google.common.net.UrlEscapers;
import com.google.inject.Inject;
import com.google.inject.Stage;

/**
 * Source of common properties and methods used within expressions.
 * 
 * @author Andreas Holstenson
 *
 */
public class CommonSource
	extends ReflectiveExpressionSource
{
	private final Escaper urlEscaper;
	private final Escaper htmlEscaper;

	@Inject
	public CommonSource(Stage stage)
	{
		super(stage);
		
		urlEscaper = UrlEscapers.urlFormParameterEscaper();
		htmlEscaper = HtmlEscapers.htmlEscaper();
	}
	
	@Property
	public Object skip()
	{
		return Attribute.ATTR_SKIP;
	}
	
	@Property
	public Object emit()
	{
		return Attribute.ATTR_EMIT;
	}
	
	@Method
	public String urlencode(String in)
	{
		return urlEscaper.escape(in);
	}
	
	@Method("urlencode")
	public String urlencodeInput(@Instance String in)
	{
		return urlEscaper.escape(in);
	}
	
	@Method
	public String htmlencode(String in)
	{
		return htmlEscaper.escape(in);
	}
	
	@Method("htmlencode")
	public String htmlencodeInput(@Instance String in)
	{
		return htmlEscaper.escape(in);
	}
}
