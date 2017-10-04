package se.l4.dust.core.internal.expression.invoke;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import com.fasterxml.classmate.ResolvedType;
import com.google.common.primitives.Primitives;

import se.l4.dust.api.Context;
import se.l4.dust.api.expression.ExpressionException;
import se.l4.dust.core.internal.expression.ErrorHandler;
import se.l4.dust.core.internal.expression.ExpressionCompiler;
import se.l4.dust.core.internal.expression.ast.Node;

public class FieldPropertyInvoker
	implements Invoker
{
	private final Node node;
	private final Field field;
	private final ResolvedType type;
	private final boolean isFinal;

	public FieldPropertyInvoker(Node node, ResolvedType type, Field field)
	{
		this.node = node;
		this.type = type;
		this.field = field;
		field.setAccessible(true);

		isFinal = Modifier.isFinal(field.getModifiers());
	}

	@Override
	public Node getNode()
	{
		return node;
	}

	@Override
	public Class<?> getReturnClass()
	{
		return type.getErasedType();
	}

	@Override
	public ResolvedType getReturnType()
	{
		return type;
	}

	@Override
	public Object get(ErrorHandler errors, Context context, Object root, Object instance)
	{
		if(instance == null)
		{
			throw errors.error(node, "Object is null, can't get a field from a null object");
		}

		try
		{
			return field.get(instance);
		}
		catch(IllegalArgumentException e)
		{
			throw errors.error(node, "Illegal argument: " + e.getMessage(), e);
		}
		catch(IllegalAccessException e)
		{
			throw errors.error(node, "Can't access the field: " + e.getMessage(), e);
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
		if(Modifier.isFinal(field.getModifiers()))
		{
			throw errors.error(node, "Field " + field + " is final");
		}

		try
		{
			field.set(instance, value);
		}
		catch(IllegalArgumentException e)
		{
			throw errors.error(node, "Illegal argument: " + e.getMessage(), e);
		}
		catch(IllegalAccessException e)
		{
			throw errors.error(node, "Can't access the field: " + e.getMessage(), e);
		}
	}

	@Override
	public boolean supportsSet()
	{
		return ! isFinal;
	}

	@Override
	public String toJavaGetter(ErrorHandler errors, ExpressionCompiler compiler, String context)
	{
		if(Modifier.isPrivate(field.getModifiers()))
		{
			String in = compiler.addInput(FieldInvoker.class, new FieldInvoker(field));
			Class<?> t = Primitives.wrap(field.getType());
			return compiler.unwrap(t, "(" + compiler.cast(t) + " " + in + ".get($2))");
		}

		return context + "." + field.getName();
	}

	@Override
	public String toJavaSetter(ErrorHandler errors, ExpressionCompiler compiler, String context)
	{
		if(Modifier.isPrivate(field.getModifiers()))
		{
			String in = compiler.addInput(FieldInvoker.class, new FieldInvoker(field));

			return in + ".set($2, $3)";
		}

		return context + "." + field.getName() + " = " + compiler.cast(field.getType()) + " $3";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((field == null) ? 0 : field.hashCode());
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
		FieldPropertyInvoker other = (FieldPropertyInvoker) obj;
		if(field == null)
		{
			if(other.field != null)
				return false;
		}
		else if(!field.equals(other.field))
			return false;
		return true;
	}

	public static class FieldInvoker
	{
		private final Field field;

		public FieldInvoker(Field field)
		{
			this.field = field;
		}

		public Object get(Object instance)
		{
			try
			{
				return field.get(instance);
			}
			catch(IllegalArgumentException e)
			{
				throw new ExpressionException(null, 0, 0, "Can't get the field: " + e.getMessage());
			}
			catch(IllegalAccessException e)
			{
				throw new ExpressionException(null, 0, 0, "Can't get the field: " + e.getMessage());
			}
		}

		public void set(Object instance, Object value)
		{
			try
			{
				field.set(instance, value);
			}
			catch(IllegalArgumentException e)
			{
				throw new ExpressionException(null, 0, 0, "Can't set the field: " + e.getMessage());
			}
			catch(IllegalAccessException e)
			{
				throw new ExpressionException(null, 0, 0, "Can't set the field: " + e.getMessage());
			}
		}
	}
}
