package se.l4.dust.api.template.spi;

import se.l4.dust.api.template.dom.DynamicContent;

public interface DynamicContentEvaluator
{
	String evaluate(DynamicContent content);
}
