package se.l4.dust.core.internal.expression.invoke;

import se.l4.dust.core.internal.expression.ErrorHandler;
import se.l4.dust.core.internal.expression.ast.AddNode;
import se.l4.dust.core.internal.expression.ast.DivideNode;
import se.l4.dust.core.internal.expression.ast.ModuloNode;
import se.l4.dust.core.internal.expression.ast.MultiplyNode;
import se.l4.dust.core.internal.expression.ast.Node;
import se.l4.dust.core.internal.expression.ast.SubtractNode;

import com.fasterxml.classmate.ResolvedType;

/**
 * Invoker for numeric operations.
 * 
 * @author Andreas Holstenson
 *
 */
public class NumericOperationInvoker
	implements Invoker
{
	private enum Operation
	{
		ADD,
		SUBTRACT,
		DIVIDE,
		MULTIPLY,
		MODULO;
		
		public double calculateFp(Number in, Number out)
		{
			switch(this)
			{
				case ADD:
					return in.doubleValue() + out.doubleValue();
				case DIVIDE:
					return in.doubleValue() / out.doubleValue();
				case MODULO:
					return in.doubleValue() % out.doubleValue();
				case MULTIPLY:
					return in.doubleValue() * out.doubleValue();
				case SUBTRACT:
					return in.doubleValue() - out.doubleValue();
			}
			
			throw new AssertionError("Unknown operation: " + this);
		}
		
		public long calculate(Number in, Number out)
		{
			switch(this)
			{
				case ADD:
					return in.longValue() + out.longValue();
				case DIVIDE:
					return in.longValue() / out.longValue();
				case MODULO:
					return in.longValue() % out.longValue();
				case MULTIPLY:
					return in.longValue() * out.longValue();
				case SUBTRACT:
					return in.longValue() - out.longValue();
			}
			
			throw new AssertionError("Unknown operation: " + this);
		}
	}
	private final Node node;
	private final Invoker left;
	private final Invoker right;
	private final Operation operation;
	private final boolean floatingPoint;

	public NumericOperationInvoker(Node node, Invoker left, Invoker right, boolean floatingPoint)
	{
		this.node = node;
		this.left = left;
		this.right = right;
		this.floatingPoint = floatingPoint;
		
		if(node instanceof AddNode)
		{
			operation = Operation.ADD;
		}
		else if(node instanceof SubtractNode)
		{
			operation = Operation.SUBTRACT;
		}
		else if(node instanceof DivideNode)
		{
			operation = Operation.DIVIDE;
		}
		else if(node instanceof MultiplyNode)
		{
			operation = Operation.MULTIPLY;
		}
		else if(node instanceof ModuloNode)
		{
			operation = Operation.MODULO;
		}
		else
		{
			throw new AssertionError("Unknown node " + node);
		}
	}
	
	@Override
	public Node getNode()
	{
		return node;
	}
	
	@Override
	public Class<?> getReturnClass()
	{
		return floatingPoint ? Double.class : Long.class;
	}
	
	@Override
	public ResolvedType getReturnType()
	{
		return null;
	}
	
	@Override
	public Object interpret(ErrorHandler errors, Object root, Object instance)
	{
		Number lv = (Number) left.interpret(errors, root, instance);
		Number rv = (Number) right.interpret(errors, root, instance);
		
		if(floatingPoint)
		{
			return operation.calculateFp(lv, rv);
		}
		
		return operation.calculate(lv, rv);
	}
	
	@Override
	public void set(ErrorHandler errors, Object root, Object instance,
			Object value)
	{
		throw errors.error(node, "Can not set value of this expression");
	}

}
