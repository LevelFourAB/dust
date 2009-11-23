package se.l4.dust.core.internal.template.dom;

/**
 * Indicate that a DOM node is location aware and can receive it's location
 * in the XML-file.
 * 
 * @author Andreas Holstenson
 *
 */
public interface LocationAware
{
	void setLocation(int line, int column);
	
	int getLine();
	
	int getColumn();
}
