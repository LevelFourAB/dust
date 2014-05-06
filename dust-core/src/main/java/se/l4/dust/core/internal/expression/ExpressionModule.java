package se.l4.dust.core.internal.expression;

import se.l4.crayon.CrayonModule;
import se.l4.crayon.annotation.Contribution;
import se.l4.dust.api.annotation.TemplateContribution;
import se.l4.dust.api.expression.Expressions;
import se.l4.dust.core.internal.conversion.ConversionModule;

/**
 * Module for expression support.
 * 
 * @author Andreas Holstenson
 *
 */
public class ExpressionModule
	extends CrayonModule
{

	@Override
	protected void configure()
	{
		install(new ConversionModule());
		
		bind(Expressions.class).to(ExpressionsImpl.class);
	}

	@TemplateContribution
	public void bindExpressionSources(Expressions expressions,
			CommonSource s1,
			VarPropertySource s2)
	{
		expressions.addSource("dust:common", s1);
		expressions.addSource("dust:variables", s2);
	}
}
