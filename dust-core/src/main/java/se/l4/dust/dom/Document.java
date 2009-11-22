package se.l4.dust.dom;

import java.util.Iterator;
import java.util.List;

import org.jdom.Content;
import org.jdom.DocType;
import org.jdom.filter.Filter;

public class Document
	extends org.jdom.Document
{

	public Document()
	{
		super();
	}

	public Document(org.jdom.Element rootElement, DocType docType, String baseURI)
	{
		super(rootElement, docType, baseURI);
	}

	public Document(org.jdom.Element rootElement, DocType docType)
	{
		super(rootElement, docType);
	}

	public Document(org.jdom.Element rootElement)
	{
		super(rootElement);
	}

	public Document(List content)
	{
		super(content);
	}

	public Object selectFirst(String xpath)
	{
		return getRootElement().selectFirst(xpath);
	}
	
	public NodeList select(String xpath)
	{
		return getRootElement().select(xpath);
	}
	
	@Override
	public Element getRootElement()
	{
		return (Element) super.getRootElement();
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
}
