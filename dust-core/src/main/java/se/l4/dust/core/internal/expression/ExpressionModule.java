package se.l4.dust.core.internal.expression;

import se.l4.crayon.CrayonModule;
import se.l4.crayon.annotation.Contribution;
import se.l4.dust.api.expression.Expressions;

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
		bind(Expressions.class).to(ExpressionsImpl.class);
	}

	@Contribution
	public void bindExpressionSources(Expressions expressions,
			CommonSource s1,
			VarProperty s2)
	{
		expressions.addSource("dust:common", s1);
		expressions.addSource("dust:variables", s2);
	}
}
