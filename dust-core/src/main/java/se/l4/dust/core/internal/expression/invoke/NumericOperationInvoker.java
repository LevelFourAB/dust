package se.l4.dust.core.internal.expression.invoke;

import java.lang.reflect.Type;

import se.l4.dust.core.internal.expression.ErrorHandler;
import se.l4.dust.core.internal.expression.ast.AddNode;
import se.l4.dust.core.internal.expression.ast.DivideNode;
import se.l4.dust.core.internal.expression.ast.ModuloNode;
import se.l4.dust.core.internal.expression.ast.MultiplyNode;
import se.l4.dust.core.internal.expression.ast.Node;
import se.l4.dust.core.internal.expression.ast.SubtractNode;

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
		
		public double calculate(Number in, Number out)
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
	}
	private final Node node;
	private final Invoker left;
	private final Invoker right;
	private final Operation operation;

	public NumericOperationInvoker(Node node, Invoker left, Invoker right)
	{
		this.node = node;
		this.left = left;
		this.right = right;
		
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
		return Number.class;
	}
	
	@Override
	public Type getReturnType()
	{
		return Number.class;
	}
	
	@Override
	public Object interpret(ErrorHandler errors, Object root, Object instance)
	{
		Number lv = (Number) left.interpret(errors, root, instance);
		Number rv = (Number) right.interpret(errors, root, instance);
		return operation.calculate(lv, rv);
	}
	
	@Override
	public void set(ErrorHandler errors, Object root, Object instance,
			Object value)
	{
		throw errors.error(node, "Can not set value of this expression");
	}

}
