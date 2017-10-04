package se.l4.dust.api.template.spi;

import java.io.IOException;

import se.l4.dust.api.resource.Resource;
import se.l4.dust.api.template.TemplateBuilder;
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
	void parse(Resource resource, TemplateBuilder builder)
		throws IOException, TemplateException;
}
