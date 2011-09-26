package se.l4.dust.core.internal.expression.invoke;

import se.l4.dust.api.expression.DynamicMethod;
import se.l4.dust.core.internal.expression.ErrorHandler;
import se.l4.dust.core.internal.expression.ExpressionCompiler;
import se.l4.dust.core.internal.expression.ast.Node;

import com.fasterxml.classmate.ResolvedType;

/**
 * Invoker for {@link DynamicMethod}.
 * 
 * @author Andreas Holstenson
 *
 */
public class DynamicMethodInvoker
	implements Invoker
{
	private final Node node;
	private final DynamicMethod method;
	private final Invoker[] params;

	public DynamicMethodInvoker(Node node, DynamicMethod method, Invoker[] params)
	{
		this.node = node;
		this.method = method;
		this.params = params;
	}

	@Override
	public Class<?> getReturnClass()
	{
		return method.getType();
	}

	@Override
	public ResolvedType getReturnType()
	{
		return null;
	}

	@Override
	public Object interpret(ErrorHandler errors, Object root, Object instance)
	{
		if(instance == null)
		{
			throw errors.error(node, "Object is null, can't invoke a method on a null object");
		}
		
		Object[] values = new Object[params.length];
		for(int i=0, n=params.length; i<n; i++)
		{
			values[i] = params[i].interpret(errors, root, root);
		}
		
		return method.invoke(null, instance, values);
	}

	@Override
	public void set(ErrorHandler errors, Object root, Object instance,
			Object value)
	{
		throw errors.error(node, "Can not set value of this expression");
	}
	
	@Override
	public String toJavaGetter(ErrorHandler errors, ExpressionCompiler compiler, String context)
	{
		String in = compiler.addInput(DynamicMethod.class, method);
		
		StringBuilder builder = new StringBuilder();
		builder.append("(")
			.append(compiler.cast(getReturnClass()))
			.append(" ")
			.append(in)
			.append(".invoke($1, ")
			.append(context);
		
		if(params.length == 0)
		{
			builder.append(", null");
		}
		else
		{
			builder.append(", new java.lang.String[] { ");
		
			for(int i=0, n=params.length; i<n; i++)
			{
				if(i > 0) builder.append(", ");
				
				String v = params[i].toJavaGetter(errors, compiler, context);
				builder.append(
					compiler.wrap(params[i].getReturnClass(), v)
				);
			}
			
			builder.append(" }");
		}
		
		builder.append("))");
		
		return builder.toString();
	}
	
	@Override
	public String toJavaSetter(ErrorHandler errors, ExpressionCompiler compiler, String context)
	{
		return null;
	}

	@Override
	public Node getNode()
	{
		return node;
	}

}
