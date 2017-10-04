package se.l4.dust.core.internal.expression.invoke;

import java.util.Arrays;

import com.fasterxml.classmate.ResolvedType;

import se.l4.dust.api.Context;
import se.l4.dust.api.expression.DynamicMethod;
import se.l4.dust.core.internal.expression.ErrorHandler;
import se.l4.dust.core.internal.expression.ExpressionCompiler;
import se.l4.dust.core.internal.expression.ast.Node;

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
	private final Class<?> context;
	private final Invoker[] params;

	public DynamicMethodInvoker(Node node, DynamicMethod method, Class<?> context, Invoker[] params)
	{
		this.node = node;
		this.method = method;
		this.context = context;
		this.params = params;
	}

	public DynamicMethod getMethod()
	{
		return method;
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
	public Object get(ErrorHandler errors, Context context, Object root, Object instance)
	{
		if(instance == null)
		{
			throw errors.error(node, "Object is null, can't invoke a method on a null object");
		}

		try
		{
			Object[] values = new Object[params.length];
			for(int i=0, n=params.length; i<n; i++)
			{
				values[i] = params[i].get(errors, context, root, root);
			}

			return method.invoke(context, instance, values);
		}
		catch(Throwable t)
		{
			throw errors.error(node, t);
		}
	}

	@Override
	public boolean supportsGet()
	{
		return true;
	}

	@Override
	public void set(ErrorHandler errors, Context context, Object root,
			Object instance, Object value)
	{
		throw errors.error(node, "Can not set value of this expression");
	}

	@Override
	public boolean supportsSet()
	{
		return false;
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
			.append(compiler.wrap(this.context, context));

		if(params.length == 0)
		{
			builder.append(", null");
		}
		else
		{
			builder.append(", new java.lang.Object[] { ");

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

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		result = prime * result + Arrays.hashCode(params);
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		DynamicMethodInvoker other = (DynamicMethodInvoker) obj;
		if(method == null)
		{
			if(other.method != null)
				return false;
		}
		else if(!method.equals(other.method))
			return false;
		if(!Arrays.equals(params, other.params))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "DynamicMethodInvoker{method=" + method + ", params=" + Arrays.toString(params) + "}";
	}
}
