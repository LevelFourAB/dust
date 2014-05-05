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
	
	/**
	 * Get the source of this content.
	 * 
	 * @return
	 */
	String getDebugSource();
	
	/**
	 * Get the line where this content originated.
	 * 
	 * @return
	 */
	int getLine();
	
	/**
	 * Get the column where this content originated.
	 * 
	 * @return
	 */
	int getColumn();
	
	/**
	 * Set debug info for this content.
	 * 
	 * @param line
	 * @param column
	 */
	void withDebugInfo(String source, int line, int column);
}
