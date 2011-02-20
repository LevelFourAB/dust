package se.l4.dust.core.internal;

import java.util.ArrayList;
import java.util.List;

import se.l4.dust.api.DocumentLinker;
import se.l4.dust.dom.Element;

public class DocumentLinkerImpl
	implements DocumentLinker
{
	private final List<Element> elements;
	
	public DocumentLinkerImpl()
	{
		elements = new ArrayList<Element>(10);
	}
	
	public void addLink(String link, String... extraArguments)
	{
		Element e = new Element("link");
		e.setAttribute("href", link);
		if(extraArguments.length % 2 != 0)
		{
			throw new IllegalArgumentException("extraArguments must be on the form: name,value,name,value");
		}
		
		for(int i=0, n=extraArguments.length; i<n; i+=2)
		{
			String name = extraArguments[i];
			String value = extraArguments[i+1];
			e.setAttribute(name, value);
		}
		
		elements.add(e);
	}

	public void addScript(String link, String... extraArguments)
	{
		// TODO Auto-generated method stub
		
	}

	public List<Element> getElements()
	{
		return elements;
	}
}
