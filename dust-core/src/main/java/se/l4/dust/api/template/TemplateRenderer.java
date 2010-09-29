package se.l4.dust.api.template;

import java.net.URL;

import org.jdom.Document;
import org.jdom.JDOMException;

/**
 * Template renderer, performs rendering of a template and returns a 
 * {@link Document}. 
 * 
 * @author Andreas Holstenson
 *
 */
public interface TemplateRenderer
{
	/**
	 * Render the template found at the specified URL using the given object
	 * as the root.
	 * 
	 * @param url
	 * @param data
	 * @return
	 * @throws JDOMException 
	 */
	Document render(URL template, Object data)
		throws JDOMException;
}
