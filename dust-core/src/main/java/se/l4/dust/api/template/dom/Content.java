package se.l4.dust.api.template.dom;

public interface Content
{
	/**
	 * Get the parent of this content.
	 */
	Element getParent();
	
	/**
	 * Set the parent of this content.
	 * 
	 * @param element
	 */
	void setParent(Element element);
	
	/**
	 * Create a copy of this content.
	 * 
	 * @return
	 */
	Content copy();
	
	/**
	 * Copy everything, including children and attributes.
	 * 
	 * @return
	 */
	Content deepCopy();
}
