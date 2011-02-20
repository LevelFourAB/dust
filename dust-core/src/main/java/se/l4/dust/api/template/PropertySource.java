package se.l4.dust.api.template;

import se.l4.dust.dom.Element;

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
	 * @param propertyExpression
	 * @param parent
	 * @return
	 */
	PropertyContent getPropertyContent(String propertyExpression, Element parent);
}
