package se.l4.dust.core.internal.expression;

import java.util.Map;

import se.l4.dust.api.Context;
import se.l4.dust.api.conversion.TypeConverter;
import se.l4.dust.api.expression.Expression;
import se.l4.dust.api.resource.ResourceLocation;
import se.l4.dust.core.internal.expression.ast.Node;
import se.l4.dust.core.internal.expression.invoke.Invoker;

/**
 * Debugger for expressions. This is used in development mode to interpret
 * expressions. Interpreted mode has error handling that should help during
 * development.
 *
 * @author Andreas Holstenson
 *
 */
public class ExpressionDebugger
	implements Expression
{
	private final Invoker invoker;
	private final ErrorHandler errors;
	private final String expression;

	private ResourceLocation debugSource;
	private int debugLine;
	private int debugColumn;

	public ExpressionDebugger(TypeConverter converter,
			ExpressionsImpl expressions,
			ResourceLocation source,
			Map<String, String> namespaces,
			String expression,
			Class<?> context)
	{
		this.expression = expression;
		Node root = ExpressionParser.parse(expression);

		this.errors = new ErrorHandlerImpl(expression);
		this.invoker = new ExpressionResolver(converter, expressions, source, namespaces, errors, root)
			.resolve(context);
	}

	@Override
	public Class<?> getType()
	{
		return invoker.getReturnClass();
	}

	@Override
	public Object get(Context context, Object instance)
	{
		return invoker.get(errors, context, instance, instance);
	}

	@Override
	public boolean supportsGet()
	{
		return invoker.supportsGet();
	}

	@Override
	public void set(Context context, Object instance, Object value)
	{
		invoker.set(errors, context, instance, instance, value);
	}

	@Override
	public boolean supportsSet()
	{
		return invoker.supportsSet();
	}

	@Override
	public String getSource()
	{
		return expression;
	}

	public void withDebugInfo(ResourceLocation source, int line, int column)
	{
		this.debugSource = source;
		this.debugLine = line;
		this.debugColumn = column;
	}
}
