package se.l4.dust.api.template;

import se.l4.dust.dom.Element;

public interface PropertySource
{
	PropertyContent getPropertyContent(String propertyExpression, Element parent);
}
