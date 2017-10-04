package se.l4.dust.core.internal.template.dom;

import se.l4.dust.api.Context;
import se.l4.dust.api.Value;
import se.l4.dust.api.expression.Expression;
import se.l4.dust.api.template.dom.Content;

/**
 * Content that will use another {@link Content} to fetch its data.
 *
 * @author Andreas Holstenson
 *
 */
public class ExpressionWithContext
	implements Value<Object>
{
	private final Expression expr;
	private final Object context;

	public ExpressionWithContext(Expression expr, Object context)
	{
		this.expr = expr;
		this.context = context;
	}

	private Object getActualContext(Context ctx, Object root)
	{
		Object context = this.context;
		if(context instanceof Value)
		{
			context = ((Value) context).get(ctx, root);
		}

		// TODO: Support for more types?
		return context;
	}

	@Override
	public Class<?> getType()
	{
		return expr.getType();
	}

	@Override
	public Object get(Context ctx, Object root)
	{
		return expr.get(ctx, getActualContext(ctx, root));
	}

	@Override
	public boolean supportsGet()
	{
		return expr.supportsGet();
	}

	@Override
	public void set(Context ctx, Object root, Object data)
	{
		expr.set(ctx, getActualContext(ctx, root), data);
	}

	@Override
	public boolean supportsSet()
	{
		return expr.supportsSet();
	}

}
