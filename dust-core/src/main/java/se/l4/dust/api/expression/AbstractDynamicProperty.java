package se.l4.dust.api.expression;

/**
 * Abstract implementation of {@link DynamicProperty}.
 * 
 * @author Andreas Holstenson
 *
 */
public abstract class AbstractDynamicProperty
	implements DynamicProperty
{
	@Override
	public boolean needsContext()
	{
		return true;
	}
	
	@Override
	public DynamicProperty getProperty(ExpressionEncounter encounter, String name)
	{
		return null;
	}
	
	@Override
	public DynamicMethod getMethod(ExpressionEncounter encounter, String name, Class... parameters)
	{
		return null;
	}
}
