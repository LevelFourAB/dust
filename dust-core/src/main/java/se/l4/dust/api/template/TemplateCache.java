package se.l4.dust.api.template;

import java.io.IOException;

import se.l4.dust.api.Context;
import se.l4.dust.api.resource.ResourceLocation;
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
	 * Get a parsed template for the given location.
	 * 
	 * @param url
	 * @return
	 * @throws IOException 
	 */
	ParsedTemplate getTemplate(Context context, Class<?> dataContext, ResourceLocation location)
		throws IOException;
	
	/**
	 * Get a parsed template for the given type.
	 * 
	 * @param context
	 * @param type
	 * @return
	 * @throws IOException
	 */
	ParsedTemplate getTemplate(Context context, Class<?> type)
		throws IOException;
}