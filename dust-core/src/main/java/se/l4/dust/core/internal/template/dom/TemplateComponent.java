package se.l4.dust.core.internal.template.dom;

import java.util.List;

import org.jdom.Attribute;
import org.jdom.Content;
import org.jdom.JDOMException;
import org.jdom.Namespace;

import se.l4.dust.api.TemplateException;
import se.l4.dust.api.template.PropertyContent;
import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.core.internal.template.components.ParameterComponent;
import se.l4.dust.core.internal.template.expression.ExpressionParser;
import se.l4.dust.dom.Element;

/**
 * Base class for all components that can be used in a template.
 * 
 * @author Andreas Holstenson
 *
 */
public abstract class TemplateComponent
	extends Element
	implements ContentPreload, LocationAware
{
	private int line;
	private int column;

	public TemplateComponent(String name, Namespace namespace)
	{
		super(name, namespace);
		
		line = column = -1;
	}
	
	public void setLocation(int line, int column)
	{
		this.line = line;
		this.column = column;
	}
	
	public int getLine()
	{
		return line;
	}
	
	public int getColumn()
	{
		return column;
	}
	
	public void preload(ExpressionParser expressionParser)
	{
		for(Attribute a : getAttributes())
		{
			if(a instanceof TemplateAttribute)
			{
				((TemplateAttribute) a).preload(expressionParser);
			}
		}
	}
	
	protected void throwException(String message)
	{
		if(line != -1)
		{
			throw new TemplateException(
				String.format("Error on line %s, column %s: %s", line, column, message)
			);
		}
		else
		{
			throw new TemplateException(message);
		}
	}

	public abstract void process(
			TemplateEmitter emitter, 
			RenderingContext ctx,
			Element parent, 
			Object root,
			TemplateComponent lastComponent,
			Object previousRoot)
		throws JDOMException;
	
	protected PropertyContent getExpressionNode(String attr, boolean required)
	{
		TemplateAttribute ta = (TemplateAttribute) getAttribute(attr);
		if(ta == null)
		{
			if(required)
			{
				throwException("Attribute " + attr + " is required");
			}
			else
			{
				return null;
			}
		}
		
		List<Content> c = ta.getContent();
		if(c.size() != 1)
		{
			throwException("Only one expression supported in attribute " + attr);
		}
		
		Content cc = c.get(0);
		if(false == cc instanceof PropertyContent)
		{
			throwException("Expected expression (${expression}) in " + attr + " but found " + cc);
		}
		
		return (PropertyContent) cc;
	}

	protected ParameterComponent getParameter(String name, boolean required)
	{
		for(Element c : getChildren())
		{
			if(c instanceof ParameterComponent)
			{
				ParameterComponent pc = (ParameterComponent) c;
				if(name.equals(pc.getAttributeValue("name")))
				{
					return pc;
				}
			}
		}
		
		if(required)
		{
			throwException("Parameter " + name + " could not be found in " + this);
		}
		
		return null;
	}
}
