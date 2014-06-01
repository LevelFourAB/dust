package se.l4.dust.api.template;

import java.io.IOException;
import java.net.URL;

import se.l4.dust.api.template.dom.ParsedTemplate;

/**
 * Template renderer, performs rendering of a template into a
 * {@link TemplateOutputStream}. 
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
	void render(RenderingContext ctx, ParsedTemplate template, Object data, TemplateOutputStream out)
		throws IOException;
	
	/**
	 * Render the template document using the given object as the root.
	 * 
	 * @param ctx
	 * @param data
	 * @param out
	 * @throws IOException
	 */
	void render(RenderingContext ctx, Object data, TemplateOutputStream out)
		throws IOException;
	
	/**
	 * Render the template document using the given object as the root.
	 * 
	 * @param ctx
	 * @param data
	 * @param out
	 * @throws IOException
	 */
	void render(RenderingContext ctx, Object data, URL template, TemplateOutputStream out)
		throws IOException;
}
