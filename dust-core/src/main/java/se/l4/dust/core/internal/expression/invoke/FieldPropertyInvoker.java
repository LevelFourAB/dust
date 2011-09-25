package se.l4.dust.core.internal.expression.invoke;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import se.l4.dust.core.internal.expression.ErrorHandler;
import se.l4.dust.core.internal.expression.ast.Node;

import com.fasterxml.classmate.ResolvedType;

public class FieldPropertyInvoker
	implements Invoker
{
	private final Node node;
	private final Field field;
	private final ResolvedType type;

	public FieldPropertyInvoker(Node node, ResolvedType type, Field field)
	{
		this.node = node;
		this.type = type;
		this.field = field;
		field.setAccessible(true);
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
	public Object interpret(ErrorHandler errors, Object root, Object instance)
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
	}
	
	@Override
	public void set(ErrorHandler errors, Object root, Object instance,
			Object value)
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
}
