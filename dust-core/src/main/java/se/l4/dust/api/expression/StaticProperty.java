package se.l4.dust.api.expression;

import se.l4.dust.api.Context;

/**
 * Static property.
 * 
 * @author Andreas Holstenson
 *
 */
public class StaticProperty
	implements DynamicProperty
{
	private final Object value;

	public StaticProperty(Object value)
	{
		this.value = value;
	}

	@Override
	public Object getValue(Context context, Object root)
	{
		return value;
	}
	
	@Override
	public void setValue(Context context, Object root, Object value)
	{
		throw new ExpressionException("setValue is unsupported for this property");
	}

	@Override
	public Class<?> getType()
	{
		return value == null ? void.class : value.getClass();
	}

}
