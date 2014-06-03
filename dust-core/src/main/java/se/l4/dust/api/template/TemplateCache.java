package se.l4.dust.api.template;

import java.io.IOException;
import java.net.URL;

import se.l4.dust.api.Context;
import se.l4.dust.api.template.dom.ParsedTemplate;

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
	 * @throws IOException 
	 */
	ParsedTemplate getTemplate(Context context, Class<?> dataContext, URL url)
		throws IOException;

	/**
	 * Get a template using the given class and annotation. If no annotation
	 * is present this will only use the class as a reference.
	 * 
	 * @param c
	 * @param annotation
	 * @return
	 * @throws IOException
	 */
	ParsedTemplate getTemplate(Context context, Class<?> dataContext, Template annotation)
		throws IOException;

}