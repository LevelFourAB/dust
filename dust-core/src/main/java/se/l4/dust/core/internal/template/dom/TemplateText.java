package se.l4.dust.core.internal.template.dom;

import java.util.List;

import org.jdom.Content;
import org.jdom.JDOMException;
import org.jdom.Text;

import se.l4.dust.core.internal.template.expression.ExpressionParser;
import se.l4.dust.dom.Element;

/**
 * Text node that supports inline expressions.
 * 
 * @author Andreas Holstenson
 *
 */
public class TemplateText
	extends Text
	implements ContentPreload, LocationAware
{
	private List<Content> content;
	private String lastValue;
	
	private int line = -1;
	private int column = -1;
	
	public TemplateText()
	{
	}
	
	public TemplateText(String text)
	{
		this();
		
		setText(text);
	}
	
	public List<Content> getContent()
		throws JDOMException
	{
		return content;
	}

	public void preload(ExpressionParser expressionParser)
	{
		content = expressionParser.parse(value, (Element) getParentElement());
	}

	public int getColumn()
	{
		return column;
	}

	public int getLine()
	{
		return line;
	}

	public void setLocation(int line, int column)
	{
		this.line = line;
		this.column = column;
	}
}
