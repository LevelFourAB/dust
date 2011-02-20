package se.l4.dust.api.template;

import java.io.IOException;
import java.net.URL;

import se.l4.dust.api.annotation.Template;
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

	/**
	 * Get a template using the given class and annotation. If no annotation
	 * is present this will only use the class as a reference.
	 * 
	 * @param c
	 * @param annotation
	 * @return
	 * @throws IOException
	 */
	Document getTemplate(Class<?> c, Template annotation) throws IOException;

}