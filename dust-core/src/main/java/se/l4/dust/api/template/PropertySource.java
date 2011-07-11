package se.l4.dust.api.template;

import se.l4.dust.api.template.dom.DynamicContent;
import se.l4.dust.api.template.dom.Element;

/**
 * Source of properties, translates expressions into {@link PropertyContent}
 * that is used when rendering templates.
 * 
 * @author Andreas Holstenson
 *
 */
public interface PropertySource
{
	/**
	 * Resolve the given expression into a {@link PropertyContent}.
	 * 
	 * @param context
	 * 		the context the property will be evaluated with
	 * @param propertyExpression
	 * 		the expression of the property
	 * @param parent
	 * 		the parent element in the template
	 * @return
	 */
	DynamicContent getPropertyContent(Class<?> context, String propertyExpression, Element parent);
}
