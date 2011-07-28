package se.l4.dust.api.template.spi;

import se.l4.dust.api.Context;
import se.l4.dust.api.resource.variant.ResourceVariant;
import se.l4.dust.api.resource.variant.ResourceVariantManager;
import se.l4.dust.api.template.dom.Content;
import se.l4.dust.api.template.dom.DynamicContent;
import se.l4.dust.api.template.dom.ParsedTemplate;

/**
 * Template variant builder used for taking a {@link ParsedTemplate} and
 * outputting a new one based on the current {@link Context}.
 * 
 * @author Andreas Holstenson
 *
 */
public interface TemplateVariant
{
	/**
	 * Get the current context.
	 * 
	 * @return
	 */
	Context getContext();
	
	/**
	 * Get the variant manager that is in use.
	 * 
	 * @return
	 */
	ResourceVariantManager getVariantManager();
	
	/**
	 * Replace the current {@link DynamicContent} with the given content.
	 * 
	 * @param content
	 * 		content to replace with
	 * @param variant
	 * 		variant of the content
	 */
	void replaceWith(Content content, ResourceVariant variant);
}
