package se.l4.dust.core.internal.expression.invoke;

import com.fasterxml.classmate.ResolvedType;
import com.google.common.primitives.Primitives;

import se.l4.dust.api.Context;
import se.l4.dust.api.expression.DynamicProperty;
import se.l4.dust.core.internal.expression.ErrorHandler;
import se.l4.dust.core.internal.expression.ExpressionCompiler;
import se.l4.dust.core.internal.expression.ast.Node;

/**
 * Invoker that wraps {@link DynamicProperty}.
 * 
 * @author Andreas Holstenson
 *
 */
public class DynamicPropertyInvoker
	implements Invoker
{
	private final Node node;
	private final DynamicProperty property;

	public DynamicPropertyInvoker(Node node, DynamicProperty property)
	{
		this.node = node;
		this.property = property;
	}

	@Override
	public Class<?> getReturnClass()
	{
		return property.getType();
	}
	
	@Override
	public ResolvedType getReturnType()
	{
		return null;
	}

	@Override
	public Object get(ErrorHandler errors, Context context, Object root, Object instance)
	{
		return property.getValue(null, instance);
	}
	
	@Override
	public void set(ErrorHandler errors, Context context, Object root,
			Object instance, Object value)
	{
		property.setValue(null, instance, value);
	}
	
	@Override
	public String toJavaGetter(ErrorHandler errors, ExpressionCompiler compiler, String context)
	{
		String id = compiler.addInput(DynamicProperty.class, property);
		Class<?> t = Primitives.wrap(getReturnClass());
		return "(" + compiler.unwrap(t, 
			compiler.cast(t) + " " + id + ".getValue($1, " + context + ")") 
			+ ")";
	}
	
	@Override
	public String toJavaSetter(ErrorHandler errors, ExpressionCompiler compiler, String context)
	{
		String id = compiler.addInput(DynamicProperty.class, property);
		return id + ".setValue($1, " + context + ", $3)"; 
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
		result = prime * result
				+ ((property == null) ? 0 : property.hashCode());
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
		DynamicPropertyInvoker other = (DynamicPropertyInvoker) obj;
		if(property == null)
		{
			if(other.property != null)
				return false;
		}
		else if(!property.equals(other.property))
			return false;
		return true;
	}
}
