package se.l4.dust.api.template.dom;

import se.l4.dust.api.resource.ResourceLocation;
import se.l4.dust.api.template.Emittable;


/**
 * Template root. This class contains information about the full template,
 * such as the DTD, root element and so on.
 * 
 * @author Andreas Holstenson
 *
 */
public class ParsedTemplate
{
	private final ResourceLocation location;
	private final String name;
	private final DocType doctype;
	private final Emittable root;
	private final Integer id;

	public ParsedTemplate(ResourceLocation location, String name, DocType doctype, Emittable root, Integer id)
	{
		this.location = location;
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
	public Emittable getRoot()
	{
		return root;
	}
	
	public Integer getRawId()
	{
		return id;
	}
	
	public ResourceLocation getLocation()
	{
		return location;
	}
}
