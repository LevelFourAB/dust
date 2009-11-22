/**
 * 
 */
package se.l4.dust.dom;

import java.util.ArrayList;

import org.jdom.JDOMException;


public class NodeList
	extends ArrayList<Object>
{
	public NodeList()
	{
	}
	
	public NodeList(int capacity)
	{
		super(capacity);
	}
	
	public NodeList select(String xpath)
		throws JDOMException
	{
		NodeList list = new NodeList();
		
		for(Object o : this)
		{
			if(o instanceof Element)
			{
				list.addAll(
					((Element) o).select(xpath)
				);
			}
		}
		
		return list;
	}
	
	public Object selectFirst(String xpath)
		throws JDOMException
	{
		for(Object o : this)
		{
			if(o instanceof Element)
			{
				Object result = ((Element) o).selectFirst(xpath);
				if(result != null)
				{
					return result;
				}
			}
		}
		
		return null;
	}
	
	public String join(String separator)
	{
		StringBuilder builder = new StringBuilder();
		boolean first = true;
		for(Object o : this)
		{
			if(first)
			{
				first = false;
			}
			else
			{
				builder.append(separator);
			}
			
			builder.append(o);
		}
		
		return builder.toString().replaceAll("\\s+", " ");
	}
}