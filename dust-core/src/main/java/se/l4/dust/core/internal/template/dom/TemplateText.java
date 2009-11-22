package se.l4.dust.core.internal.template.dom;

import java.util.List;

import org.jdom.Content;
import org.jdom.JDOMException;
import org.jdom.Text;

/**
 * Text node that supports inline expressions.
 * 
 * @author Andreas Holstenson
 *
 */
public class TemplateText
	extends Text
	implements ContentPreload
{
	private List<Content> content;
	private String lastValue;
	
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

	public void preload()
	{
		content = ExpressionParser.parse(value);
	}
}
