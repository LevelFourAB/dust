package se.l4.dust.api.template.dom;

import java.util.Collection;

import se.l4.dust.api.TemplateException;
import se.l4.dust.api.template.RenderingContext;

/**
 * Element abstraction. This is used to represent tags and components in the
 * parsed template.
 * 
 * @author Andreas Holstenson
 *
 */
public class Element
	implements Content
{
	private static final Content[] EMPTY_OBJECTS = new Content[0];
	private static final Attribute[] EMPTY_ATTRS = new Attribute[0];
	
	private final String name;
	private Attribute[] attributes;
	private Content[] contents;
	private Element parent;
	
	public Element(String name, String... attributes)
	{
		this.name = name;
		
		if(attributes.length % 2 != 0)
		{
			throw new IllegalArgumentException("Attributes must be given as key, value (in pairs)");
		}
		
		contents = EMPTY_OBJECTS;
		
		int len = attributes.length / 2;
		this.attributes = len == 0 ? EMPTY_ATTRS : new Attribute[len];
		for(int i=0; i<attributes.length; i+=2)
		{
			this.attributes[i/2] = new Attribute(attributes[i], new Text(attributes[i+1]));
		}
	}
	
	private Element(String name, Attribute[] attributes)
	{
		this.name = name;
		this.attributes = attributes;
		
		contents = EMPTY_OBJECTS;
	}
	
	public Content copy()
	{
		return new Element(name, attributes);
	}
	
	public Element copyAttributes(Element other)
	{
		this.attributes = other.attributes;
		return this;
	}
	
	/**
	 * Add content to this element, content may be anything that implements
	 * {@link Content}.
	 * 
	 * @param object
	 * @return
	 */
	public Element addContent(Content object)
	{
		Content[] result = new Content[contents.length + 1];
		System.arraycopy(contents, 0, result, 0, contents.length);
		result[contents.length] = object;
		object.setParent(this);
		
		contents = result;
		
		return this;
	}
	
	/**
	 * Add content to this element, see {@link #addContent(Content)}.
	 * 
	 * @param objects
	 * @return
	 */
	public Element addContent(Collection<Content> objects)
	{
		Content[] result = new Content[contents.length + objects.size()];
		System.arraycopy(contents, 0, result, 0, contents.length);
		
		int index = contents.length;
		for(Content o : objects)
		{
			result[index++] = o;
			o.setParent(this);
		}
		
		contents = result;
		
		return this;
	}
	
	public Element setAttribute(String name, Content... args)
	{
		Attribute[] result = new Attribute[attributes.length + 1];
		System.arraycopy(attributes, 0, result, 0, attributes.length);
		result[attributes.length] = new Attribute(name, args);
		attributes = result;
		
		return this;
	}
	
	public String getName()
	{
		return name;
	}
	
	public Attribute[] getAttributes()
	{
		return attributes;
	}
	
	public Element getParent()
	{
		return parent;
	}
	
	public void setParent(Element element)
	{
		parent = element;
	}
	
	public Content[] getRawContents()
	{
		return contents;
	}
	
	@Override
	public String toString()
	{
		return "Element[" + name + "]";
	}
	
	public Content[] getAttributeValue(String name)
	{
		for(Attribute a : attributes)
		{
			if(a.getName().equals(name))
			{
				return a.getValue();
			}
		}
		
		return null;
	}
	
	public Attribute getAttribute(String name)
	{
		for(Attribute a : attributes)
		{
			if(a.getName().equals(name))
			{
				return a;
			}
		}
		
		return null;
	}
	
	public String findNamespace(String prefix)
	{
		String attr = "xmlns:" + prefix;
		Element e = this;
		while(e != null)
		{
			Element.Attribute a = e.getAttribute(attr);
			if(a != null)
			{
				return a.getStringValue();
			}
			e = e.getParent();
		}
		
		return null;
	}
	
	public static class Attribute
	{
		public static final String ATTR_EMIT = "##emit";
		public static final String ATTR_SKIP = "##skip";
		
		private final String name;
		private final Content[] value;

		public Attribute(String name, Content... value)
		{
			this.name = name;
			this.value = value;
		}
		
		public String getName()
		{
			return name;
		}
		
		public Content[] getValue()
		{
			return value;
		}
		
		public Object getValue(RenderingContext ctx, Object root)
		{
			if(value.length == 1)
			{
				Content c = value[0];
				return getValueOf(ctx, root, c);
			}
			else
			{
				StringBuilder result = new StringBuilder();
				for(Content c : value)
				{
					Object value = getValueOf(ctx, root, c);
					result.append(ctx.getStringValue(value));
				}
				
				return result.toString();
			}
		}

		private Object getValueOf(RenderingContext ctx, Object root, Content c)
		{
			if(c instanceof DynamicContent)
			{
				return ctx.getDynamicValue((DynamicContent) c, root);
			}
			else if(c instanceof Text)
			{
				return ((Text) c).getText();
			}
			else
			{
				return "";
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

		public String getStringValue()
		{
			StringBuilder builder = new StringBuilder();
			for(Content c : value)
			{
				if(c instanceof Text)
				{
					builder.append(((Text) c).getText());
				}
			}
			
			return builder.toString();
		}
		
		public void setValue(RenderingContext ctx, Object root, Object value)
		{
			if(this.value.length != 1)
			{
				throw new TemplateException("Unable to set value of attribute " + name + ", contains more than one expression");
			}
			
			Content c = this.value[0];
			if(c instanceof DynamicContent)
			{
				((DynamicContent) c).setValue(ctx, root, value);
			}
			else
			{
				throw new TemplateException("Unable to set value of attribute " + name + ", does not contain any dynamic content");
			}
		}
	}
	
}
