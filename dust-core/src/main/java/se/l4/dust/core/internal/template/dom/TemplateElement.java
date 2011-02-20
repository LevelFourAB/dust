package se.l4.dust.core.internal.template.dom;

import java.util.ArrayList;

import org.jdom.Attribute;
import org.jdom.Namespace;

import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.dom.Element;

public class TemplateElement
	extends Element
	implements ContentPreload, LocationAware
{
	private int line = -1;
	private int column = -1;
	
	public TemplateElement()
	{
		super();
	}

	public TemplateElement(String name, Namespace namespace)
	{
		super(name, namespace);
	}

	public TemplateElement(String name, String prefix, String uri)
	{
		super(name, prefix, uri);
	}

	public TemplateElement(String name, String uri)
	{
		super(name, uri);
	}

	public TemplateElement(String name)
	{
		super(name);
	}
	
	public void setLocation(int line, int column)
	{
		this.line = line;
		this.column = column;
	}
	
	public int getColumn()
	{
		return column;
	}
	
	public int getLine()
	{
		return line;
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
	
	public TemplateElement copy(RenderingContext ctx, Object root)
	{
		TemplateElement copy = new TemplateElement(getName(), getNamespace());

		for(Attribute a : getAttributes())
		{
			if(a instanceof TemplateAttribute)
			{
				String value = ((TemplateAttribute) a).getStringValue(ctx, root);
				copy.setAttribute(
					new Attribute(a.getName(), value, a.getAttributeType(), a.getNamespace())
				);
			}
			else
			{
				copy.setAttribute((Attribute) a.clone());
			}
		}
		
		if(additionalNamespaces != null)
		{
			copy.additionalNamespaces = new ArrayList<Namespace>(additionalNamespaces);
		}

		return copy;
	}
}
