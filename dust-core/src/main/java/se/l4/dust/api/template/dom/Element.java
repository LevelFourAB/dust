package se.l4.dust.api.template.dom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import se.l4.dust.api.template.Emittable;

/**
 * Element abstraction. This is used to represent tags and components in the
 * parsed template.
 *
 * @author Andreas Holstenson
 *
 */
public abstract class Element
	extends AbstractContent
{
	private static final Content[] EMPTY_OBJECTS = new Content[0];
	private static final Attribute<?>[] EMPTY_ATTRS = new Attribute[0];

	protected final String name;

	protected Attribute<?>[] attributes;
	protected Emittable[] contents;

	private Map<String, Emittable> parameters;

	public Element(String name)
	{
		this.name = name;

		contents = EMPTY_OBJECTS;

		this.attributes = EMPTY_ATTRS;
	}

	/**
	 * Add content to this element, content may be anything that implements
	 * {@link Content}.
	 *
	 * @param object
	 * @return
	 */
	public void addContent(Emittable object)
	{
		Emittable[] result = new Emittable[contents.length + 1];
		System.arraycopy(contents, 0, result, 0, contents.length);
		result[contents.length] = object;

		contents = result;
	}

	/**
	 * Add content to this element, see {@link #addContent(Content)}.
	 *
	 * @param objects
	 * @return
	 */
	public void addContent(Iterable<? extends Emittable> objects)
	{
		List<Emittable> result = new ArrayList<>(contents.length + 10);
		for(Emittable e : contents)
		{
			result.add(e);
		}

		for(Emittable e : objects)
		{
			result.add(e);
		}

		contents = result.toArray(new Emittable[result.size()]);
	}

	/**
	 * Prepend content to this element.
	 *
	 * @param objects
	 * @return
	 */
	public void prependContent(Collection<? extends Emittable> objects)
	{
		Emittable[] result = new Emittable[contents.length + objects.size()];
		System.arraycopy(contents, 0, result, objects.size(), contents.length);

		int index = 0;
		for(Emittable o : objects)
		{
			result[index++] = o;
		}

		contents = result;
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

	public void addAttribute(Attribute<?> attribute)
	{
		// Check for existing attribute
		for(int i=0, n=attributes.length; i<n; i++)
		{
			if(attributes[i].getName().equals(name))
			{
				// Replace the attribute
				attributes[i] = attribute;
				return;
			}
		}

		// New attribute
		Attribute[] result = new Attribute[attributes.length + 1];
		System.arraycopy(attributes, 0, result, 0, attributes.length);
		result[attributes.length] = attribute;
		attributes = result;
	}

	public String getName()
	{
		return name;
	}

	public Attribute<?>[] getAttributes()
	{
		return attributes;
	}

	public Emittable[] getRawContents()
	{
		return contents;
	}

	@Override
	public String toString()
	{
		return "Element[" + name + "]";
	}

	public Attribute<?> getAttribute(String name)
	{
		for(Attribute<?> a : attributes)
		{
			if(a.getName().equals(name))
			{
				return a;
			}
		}

		return null;
	}

//	public void setAttributes(AttributeImpl[] attributes)
//	{
//		this.attributes = attributes;
//	}

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


	public void adoptParameters(Element e)
	{
		parameters = e.parameters;
	}
}
