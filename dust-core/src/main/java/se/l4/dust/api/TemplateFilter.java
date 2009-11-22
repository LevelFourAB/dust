package se.l4.dust.api;

import org.jdom.JDOMException;

import se.l4.dust.dom.Document;

/**
 * Interface to implement support for filtering document output from templates.
 * This interface is used to modify the template after the template has 
 * rendered but before it is sent to the client.
 * 
 * @author Andreas Holstenson
 *
 */
public interface TemplateFilter
{
	/**
	 * Perform filtering on the given document.
	 * 
	 * @param document
	 * @throws JDOMException
	 */
	void filter(Document document)
		throws JDOMException;
}
