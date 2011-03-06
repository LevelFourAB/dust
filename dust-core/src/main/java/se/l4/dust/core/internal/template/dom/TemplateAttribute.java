package se.l4.dust.core.internal.template.dom;

import java.util.List;

import org.jdom.Attribute;
import org.jdom.Content;
import org.jdom.Namespace;

import se.l4.dust.api.template.PropertyContent;
import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.core.internal.template.expression.ExpressionParser;
import se.l4.dust.dom.Element;

public class TemplateAttribute
	extends Attribute
	implements ContentPreload
{
	private List<Content> content;
	private String lastValue;
	
	public TemplateAttribute()
	{
		super();
	}

	public TemplateAttribute(String name, String value, int type,
			Namespace namespace)
	{
		super(name, value, type, namespace);
	}

	public TemplateAttribute(String name, String value, int type)
	{
		super(name, value, type);
	}

	public TemplateAttribute(String name, String value, Namespace namespace)
	{
		super(name, value, namespace);
	}

	public TemplateAttribute(String name, String value)
	{
		super(name, value);
	}

	public List<Content> getContent()
	{
		return content;
	}

	public void preload(ExpressionParser expressionParser)
	{
		content = expressionParser.parse(value, (Element) getParent());
	}
	
	public Object getValue(RenderingContext ctx, Object root)
	{
		if(content.size() == 1)
		{
			Content c = content.get(0);
			if(c instanceof PropertyContent)
			{
				return ((PropertyContent) c).getValue(ctx, root);
			}
			else
			{
				return c.getValue();
			}
		}
		else
		{
			StringBuilder value = new StringBuilder();
			for(Content c : content)
			{
				if(c instanceof PropertyContent)
				{
					value.append(((PropertyContent) c).getValue(ctx, root));
				}
				else
				{
					value.append(c.getValue());
				}
			}
			
			return value.toString();
		}
	}

	public String getStringValue(RenderingContext ctx, Object root)
	{
		Object o = getValue(ctx, root);
		if(o == null)
		{
			return "null";
		}
		else
		{
			return o.toString();
		}
	}
}
