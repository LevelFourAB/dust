package se.l4.dust.api.template.dom;

import se.l4.dust.api.template.Emittable;

public interface Content
	extends Emittable
{
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
