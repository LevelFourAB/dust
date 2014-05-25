package se.l4.dust.api.template.spi;

import java.io.IOException;
import java.io.InputStream;

import se.l4.dust.api.template.TemplateException;

/**
 * Template parser abstraction.
 * 
 * @author Andreas Holstenson
 *
 */
public interface TemplateParser
{
	/**
	 * Parse the given input stream outputting a new template.
	 * 
	 * @param stream
	 * @param builder
	 * @throws IOException
	 * @throws TemplateException
	 */
	void parse(InputStream stream, String name, TemplateBuilder builder)
		throws IOException, TemplateException;
}
