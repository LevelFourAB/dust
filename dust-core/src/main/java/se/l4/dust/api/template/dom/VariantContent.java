package se.l4.dust.api.template.dom;

import se.l4.dust.api.Context;
import se.l4.dust.api.resource.variant.ResourceVariant;
import se.l4.dust.api.template.spi.TemplateVariant;

/**
 * Marker interface for {@link DynamicContent} that may vary depending
 * on {@link Context}. This is used together with {@link ResourceVariant}s
 * 
 * @author Andreas Holstenson
 *
 */
public interface VariantContent
{
	/**
	 * Transform the content based on the given context.
	 * 
	 * @param context
	 * @return
	 */
	void transform(TemplateVariant variant);
}
