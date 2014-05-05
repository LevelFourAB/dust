package se.l4.dust.api.template.dom;


/**
 * Template root. This class contains information about the full template,
 * such as the DTD, root element and so on.
 * 
 * @author Andreas Holstenson
 *
 */
public class ParsedTemplate
{
	private final String name;
	private final DocType doctype;
	private final Element root;
	private final Integer id;

	public ParsedTemplate(String name, DocType doctype, Element root, Integer id)
	{
		this.name = name;
		this.doctype = doctype;
		this.root = root;
		this.id = id;
	}
	
	public String getName()
	{
		return name;
	}
	
	/**
	 * Get the document type (if any).
	 * 
	 * @return
	 */
	public DocType getDocType()
	{
		return doctype;
	}
	
	/**
	 * Get the root element.
	 * 
	 * @return
	 */
	public Element getRoot()
	{
		return root;
	}
	
	public Integer getRawId()
	{
		return id;
	}
}
