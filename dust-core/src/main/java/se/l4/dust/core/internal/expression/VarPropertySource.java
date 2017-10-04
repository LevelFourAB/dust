package se.l4.dust.core.internal.expression;

import se.l4.dust.api.Context;
import se.l4.dust.api.expression.AbstractDynamicProperty;
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
public class VarPropertySource
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
		return encounter.isRoot() ? new Content(name) : null;
	}

	public static class Content
		extends AbstractDynamicProperty
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
		public Object get(Context ctx, Object root)
		{
			return ctx.getValue(compoundKey);
		}

		@Override
		public boolean supportsGet()
		{
			return true;
		}

		@Override
		public void set(Context context, Object root, Object value)
		{
			context.putValue(compoundKey, value);
		}

		@Override
		public boolean supportsSet()
		{
			return true;
		}
	}
}
