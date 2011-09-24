package se.l4.dust.core.internal.expression;

import se.l4.dust.api.Context;
import se.l4.dust.api.expression.DynamicMethod;
import se.l4.dust.api.expression.DynamicProperty;
import se.l4.dust.api.expression.ExpressionEncounter;
import se.l4.dust.api.expression.ExpressionSource;

/**
 * Source of variable properties.
 * 
 * @author Andreas Holstenson
 *
 */
public class VarProperty
	implements ExpressionSource
{

	@Override
	public DynamicMethod getMethod(ExpressionEncounter encounter, String name, Class... parameters)
	{
		return null;
	}
	
	@Override
	public DynamicProperty getProperty(ExpressionEncounter encounter, String name)
	{
		return new Content(name);
	}

	public static class Content
		implements DynamicProperty
	{
		private final String key;
		private final String compoundKey;
	
		public Content(String key)
		{
			this.key = key;
			this.compoundKey = "var:" + key;
		}
		
		@Override
		public Class<?> getType()
		{
			return Object.class;
		}
		
		@Override
		public Object getValue(Context ctx, Object root)
		{
			return ctx.getValue(compoundKey);
		}
	}
}
