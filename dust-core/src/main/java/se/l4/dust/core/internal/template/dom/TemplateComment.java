package se.l4.dust.core.internal.template.dom;

import java.util.List;

import org.jdom.Comment;
import org.jdom.Content;
import org.jdom.JDOMException;

import se.l4.dust.core.internal.template.expression.ExpressionParser;
import se.l4.dust.dom.Element;

public class TemplateComment
	extends Comment
	implements ContentPreload, LocationAware
{
	private List<Content> content;
	private String lastValue;
	
	private int line = -1;
	private int column = -1;
	
	public TemplateComment()
	{
	}
	
	public TemplateComment(String text)
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
		content = expressionParser.parse(text, (Element) getParentElement());
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
