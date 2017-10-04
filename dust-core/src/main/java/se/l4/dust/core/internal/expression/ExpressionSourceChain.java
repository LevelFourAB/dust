package se.l4.dust.core.internal.expression;

import se.l4.dust.api.expression.DynamicMethod;
import se.l4.dust.api.expression.DynamicProperty;
import se.l4.dust.api.expression.ExpressionEncounter;
import se.l4.dust.api.expression.ExpressionSource;

/**
 * Chaining of two {@link ExpressionSource}s.
 *
 * @author Andreas Holstenson
 *
 */
public class ExpressionSourceChain
	implements ExpressionSource
{
	private final ExpressionSource left;
	private final ExpressionSource right;

	public ExpressionSourceChain(ExpressionSource left, ExpressionSource right)
	{
		this.left = left;
		this.right = right;
	}

	@Override
	public DynamicProperty getProperty(ExpressionEncounter encounter, String name)
	{
		DynamicProperty property = left.getProperty(encounter, name);
		if(property != null) return property;

		return right.getProperty(encounter, name);
	}

	@Override
	public DynamicMethod getMethod(ExpressionEncounter encounter, String name, Class... parameters)
	{
		DynamicMethod method = left.getMethod(encounter, name, parameters);
		if(method != null) return method;

		return right.getMethod(encounter, name, parameters);
	}

	@Override
	public String toString()
	{
		return "ExpressionSourceChain{" + left + " and " + right + "}";
	}
}
