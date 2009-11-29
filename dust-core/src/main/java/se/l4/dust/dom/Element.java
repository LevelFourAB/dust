package se.l4.dust.dom;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.Content;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.Text;
import org.jdom.filter.Filter;
import org.jdom.xpath.XPath;

public class Element
	extends org.jdom.Element
{
	public Element()
	{
		super();
	}

	public Element(String name, Namespace namespace)
	{
		super(name, namespace);
	}

	public Element(String name, String prefix, String uri)
	{
		super(name, prefix, uri);
	}

	public Element(String name, String uri)
	{
		super(name, uri);
	}

	public Element(String name)
	{
		super(name);
	}

	/**
	 * Select the first thing that matches the given XPath. Can be either
	 * {@link Element}, {@link Attribute}, {@link Text}, @{link CDATA} or
	 * {@link String}, {@link Boolean}, {@link Double}.
	 * 
	 * @param xpath
	 * 		xpath to run
	 * @return
	 * 		first object that mathces, or {@code null}
	 */
	public Object selectFirst(String xpath)
	{
		try
		{
			XPath processor = XPathScraper.getXPath(xpath);
			
			return processor.selectSingleNode(this);
		}
		catch(JDOMException e)
		{
			return null;
		}
	}
	
	public NodeList select(String xpath)
	{
		try
		{
			XPath path = XPathScraper.getXPath(xpath);
			List<?> nodes = path.selectNodes(this);
		
			NodeList result = new NodeList(nodes.size());
			for(Object o : nodes)
			{
				result.add(o);
			}
		
			return result;
		}
		catch(JDOMException e)
		{
			return new NodeList();
		}
	}
	
	@Override
	public Element getChild(String name)
	{
		return (Element) super.getChild(name);
	}
	
	@Override
	public Element getChild(String name, Namespace ns)
	{
		return (Element) super.getChild(name, ns);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Element> getChildren()
	{
		return super.getChildren();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Element> getChildren(String name)
	{
		return super.getChildren(name);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Element> getChildren(String name, Namespace ns)
	{
		return super.getChildren(name, ns);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Attribute> getAttributes()
	{
		return super.getAttributes();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Content> getContent()
	{
		return super.getContent();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Content> getContent(Filter filter)
	{
		return super.getContent(filter);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Iterator<Content> getDescendants()
	{
		return super.getDescendants();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Iterator<Content> getDescendants(Filter filter)
	{
		return super.getDescendants(filter);
	}
	
	@Override
	public List<Namespace> getAdditionalNamespaces()
	{
		return super.getAdditionalNamespaces();
	}
	
	/**
	 * Create a copy of this element without its content.
	 * 
	 * @return
	 */
	public Element copy()
	{
		Element copy = new Element(getName(), getNamespace());

		for(Attribute a : getAttributes())
		{
			copy.setAttribute((Attribute) a.clone());
		}
		
		if(additionalNamespaces != null)
		{
			copy.additionalNamespaces = new ArrayList<Namespace>(additionalNamespaces);
		}

		return copy;
	}	 
}
