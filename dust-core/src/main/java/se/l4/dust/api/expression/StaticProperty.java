package se.l4.dust.api.expression;

import se.l4.dust.api.Context;

/**
 * Static property.
 * 
 * @author Andreas Holstenson
 *
 */
public class StaticProperty<T>
	extends AbstractDynamicProperty<T>
{
	private final T value;

	public StaticProperty(T value)
	{
		this.value = value;
	}

	@Override
	public T get(Context context, Object root)
	{
		return value;
	}
	
	@Override
	public boolean supportsGet()
	{
		return true;
	}
	
	@Override
	public void set(Context context, Object root, Object value)
	{
		throw new ExpressionException("set is unsupported for this property");
	}
	
	@Override
	public boolean supportsSet()
	{
		return false;
	}

	@Override
	public Class<? extends T> getType()
	{
		return (Class<? extends T>) value.getClass();
	}
}
