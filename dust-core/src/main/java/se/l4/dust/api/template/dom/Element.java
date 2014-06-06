package se.l4.dust.api.template.dom;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import se.l4.dust.api.template.Emittable;
import se.l4.dust.api.template.TemplateEmitter;
import se.l4.dust.api.template.TemplateOutputStream;

import com.google.common.collect.Maps;

/**
 * Element abstraction. This is used to represent tags and components in the
 * parsed template.
 * 
 * @author Andreas Holstenson
 *
 */
public class Element
	extends AbstractContent
{
	private static final Content[] EMPTY_OBJECTS = new Content[0];
	private static final Attribute[] EMPTY_ATTRS = new Attribute[0];
	
	private final String name;
	
	protected Attribute[] attributes;
	protected Emittable[] contents;
	
	private Map<String, Emittable> parameters;
	
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
	public Element addContent(Emittable object)
	{
		Emittable[] result = new Emittable[contents.length + 1];
		System.arraycopy(contents, 0, result, 0, contents.length);
		result[contents.length] = object;
		
		contents = result;
		
		return this;
	}
	
	/**
	 * Add content to this element, see {@link #addContent(Content)}.
	 * 
	 * @param objects
	 * @return
	 */
	public Element addContent(Collection<? extends Emittable> objects)
	{
		Emittable[] result = new Emittable[contents.length + objects.size()];
		System.arraycopy(contents, 0, result, 0, contents.length);
		
		int index = contents.length;
		for(Emittable o : objects)
		{
			result[index++] = o;
		}
		
		contents = result;
		
		return this;
	}
	
	/**
	 * Prepend content to this element.
	 * 
	 * @param objects
	 * @return
	 */
	public Element prependContent(Collection<? extends Emittable> objects)
	{
		Emittable[] result = new Emittable[contents.length + objects.size()];
		System.arraycopy(contents, 0, result, objects.size(), contents.length);
		
		int index = 0;
		for(Emittable o : objects)
		{
			result[index++] = o;
		}
		
		contents = result;
		
		return this;
	}
	
	public void replaceContent(Emittable existing, Emittable newContent)
	{
		for(int i=0, n=contents.length; i<n; i++)
		{
			if(contents[i] == existing)
			{
				contents[i] = newContent;
				return;
			}
		}
		
		throw new IllegalArgumentException("No such content");
	}
	
	public void setContents(Content[] newContent)
	{
		this.contents = newContent;
	}
	
	public Element setAttribute(String name, Content... args)
	{
		// Check for existing attribute
		for(int i=0, n=attributes.length; i<n; i++)
		{
			if(attributes[i].getName().equals(name))
			{
				// Replace the attribute
				attributes[i] = new Attribute(name, args);
				return this;
			}
		}
		
		// New attribute
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
	
	public Emittable[] getRawContents()
	{
		return contents;
	}
	
	@Override
	public void emit(TemplateEmitter emitter, TemplateOutputStream output)
		throws IOException
	{
		Emittable[] content = getRawContents();
		String[] attrs = emitter.createAttributes(this);
		
		output.startElement(name, attrs, false);
		
		emitter.emit(content);
		
		output.endElement(name);
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
	
	public void setAttributes(Attribute[] attributes)
	{
		this.attributes = attributes;
	}
	
	@Override
	public int getLine()
	{
		return line;
	}
	
	@Override
	public int getColumn()
	{
		return column;
	}
	
	public Emittable getParameter(String name)
	{
		if(parameters == null) return null;
		
		return parameters.get(name);
	}
	
	public void addParameter(String name, Emittable content)
	{
		if(parameters == null) parameters = Maps.newHashMap();
		
		parameters.put(name, content);
	}

	

}
