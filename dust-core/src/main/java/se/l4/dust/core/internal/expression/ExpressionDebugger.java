package se.l4.dust.core.internal.expression;

import se.l4.dust.api.conversion.TypeConverter;
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
{
	private final Invoker invoker;
	private final ErrorHandler errors;

	public ExpressionDebugger(TypeConverter converter, String expression, Class<?> context)
	{
		Node root = ExpressionParser.parse(expression);
		
		this.errors = new ErrorHandler(expression);
		this.invoker = new ExpressionResolver(converter, errors, root).resolve(context);
	}
	
	public Object execute(Object instance)
	{
		return invoker.interpret(errors, instance, instance);
	}
}
