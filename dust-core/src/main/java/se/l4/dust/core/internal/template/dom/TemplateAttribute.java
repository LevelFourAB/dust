package se.l4.dust.core.internal.template.dom;

import java.util.List;

import org.jdom.Attribute;
import org.jdom.Content;
import org.jdom.Namespace;

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

	public void preload()
	{
		content = ExpressionParser.parse(value);
	}
	
	public Object getValue(Object root)
	{
		if(content.size() == 1)
		{
			Content c = content.get(0);
			if(c instanceof ExpressionNode)
			{
				return ((ExpressionNode) c).getValue(root);
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
				if(c instanceof ExpressionNode)
				{
					value.append(((ExpressionNode) c).getValue(root));
				}
				else
				{
					value.append(c.getValue());
				}
			}
			
			return value.toString();
		}
	}

	public String getStringValue(Object root)
	{
		Object o = getValue(root);
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
