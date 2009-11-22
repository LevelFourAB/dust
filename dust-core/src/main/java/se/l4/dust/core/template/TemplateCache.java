package se.l4.dust.core.template;

import java.net.URL;

import se.l4.dust.dom.Document;

/**
 * Cache for templates in the system.
 * 
 * @author Andreas Holstenson
 *
 */
public interface TemplateCache
{
	/**
	 * Get a parsed template for the given URL.
	 * 
	 * @param url
	 * @return
	 */
	Document getTemplate(URL url);

}