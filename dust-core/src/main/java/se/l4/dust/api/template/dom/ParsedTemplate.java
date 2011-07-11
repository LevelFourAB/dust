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
	private final DocType doctype;
	private final Element root;

	public ParsedTemplate(DocType doctype, Element root)
	{
		this.doctype = doctype;
		this.root = root;
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
}
