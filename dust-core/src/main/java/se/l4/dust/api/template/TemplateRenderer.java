package se.l4.dust.api.template;

import java.net.URL;

import org.jdom.JDOMException;

import se.l4.dust.dom.Document;

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
	 * Render the template document using the given object as the root.
	 * 
	 * @param ctx
	 * 		the context to use
	 * @param url
	 * 		url of the template
	 * @param data
	 * 		data to use as template root
	 * @return
	 * @throws JDOMException 
	 */
	Document render(TemplateContext ctx, Document template, Object data)
		throws JDOMException;
	
	/**
	 * Render the template found at the specified URL using the given object
	 * as the root.
	 * 
	 * @param ctx
	 * 		the context to use
	 * @param url
	 * 		url of the template
	 * @param data
	 * 		data to use as template root
	 * @return
	 * @throws JDOMException 
	 */
	Document render(TemplateContext ctx, URL template, Object data)
		throws JDOMException;
}
