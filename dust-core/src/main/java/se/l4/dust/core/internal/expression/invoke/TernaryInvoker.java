package se.l4.dust.core.internal.expression.invoke;

import se.l4.dust.core.internal.expression.ErrorHandler;
import se.l4.dust.core.internal.expression.ExpressionCompiler;
import se.l4.dust.core.internal.expression.ast.Node;

import com.fasterxml.classmate.ResolvedType;

/**
 * Invoker for ternary ifs.
 * 
 * @author Andreas Holstenson
 *
 */
public class TernaryInvoker
	implements Invoker
{
	private final Node node;
	private final Invoker test;
	private final Invoker left;
	private final Invoker right;

	public TernaryInvoker(Node node, Invoker test, Invoker left, Invoker right)
	{
		this.node = node;
		this.test = test;
		this.left = left;
		this.right = right;
	}
	
	@Override
	public Node getNode()
	{
		return node;
	}
	
	@Override
	public Class<?> getReturnClass()
	{
		if(right == null)
		{
			return left.getReturnClass();
		}
		else if(left.getReturnClass().isAssignableFrom(right.getReturnClass()))
		{
			return left.getReturnClass();
		}
		else if(right.getReturnClass().isAssignableFrom(left.getReturnClass()))
		{
			return right.getReturnClass();
		}
		
		// TODO: Better guessing for the return type
		return Object.class;
	}
	
	@Override
	public ResolvedType getReturnType()
	{
		if(right == null)
		{
			return left.getReturnType();
		}
		else if(left.getReturnClass().isAssignableFrom(right.getReturnClass()))
		{
			return left.getReturnType();
		}
		else if(right.getReturnClass().isAssignableFrom(left.getReturnClass()))
		{
			return right.getReturnType();
		}
		
		// TODO: Better guessing for the return type
		return null;
	}
	
	@Override
	public Object interpret(ErrorHandler errors, Object root, Object instance)
	{
		Object result = test.interpret(errors, root, instance);
		if(Boolean.TRUE.equals(result))
		{
			return left.interpret(errors, root, instance);
		}
		else if(right == null)
		{
			return null;
		}
		else
		{
			return right.interpret(errors, root, instance);
		}
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
		return "(" + compiler.cast(getReturnClass()) + " (" 
			+ test.toJavaGetter(errors, compiler, context)
			+ " ? "
			+ left.toJavaGetter(errors, compiler, context)
			+ " : " + (right == null ? "null" : right.toJavaGetter(errors, compiler, context))
			+ "))";
	}
	
	@Override
	public String toJavaSetter(ErrorHandler errors, ExpressionCompiler compiler, String context)
	{
		return null;
	}
}
