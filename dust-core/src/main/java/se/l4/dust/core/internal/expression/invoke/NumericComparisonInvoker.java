package se.l4.dust.core.internal.expression.invoke;

import se.l4.dust.core.internal.expression.ErrorHandler;
import se.l4.dust.core.internal.expression.ast.GreaterNode;
import se.l4.dust.core.internal.expression.ast.GreaterOrEqualNode;
import se.l4.dust.core.internal.expression.ast.LessNode;
import se.l4.dust.core.internal.expression.ast.LessOrEqualNode;
import se.l4.dust.core.internal.expression.ast.Node;

public class NumericComparisonInvoker
	extends AbstractBooleanInvoker
{
	private enum Comparator
	{
		LESS,
		LESS_OR_EQUAL,
		GREATER,
		GREATER_OR_EQUAL;
		
		public boolean check(Number lv, Number rv)
		{
			switch(this)
			{
				case GREATER:
					return lv.doubleValue() > rv.doubleValue();
				case GREATER_OR_EQUAL:
					return lv.doubleValue() >= rv.doubleValue();
				case LESS:
					return lv.doubleValue() < rv.doubleValue();
				case LESS_OR_EQUAL:
					return lv.doubleValue() <= rv.doubleValue();
			}
			
			throw new AssertionError("Unknown comparison type " + this);
		}
	}

	private final Comparator comparator;
	
	public NumericComparisonInvoker(Node node, Invoker left, Invoker right)
	{
		super(node, left, right);
		
		if(node instanceof LessNode)
		{
			comparator = Comparator.LESS;
		}
		else if(node instanceof LessOrEqualNode)
		{
			comparator = Comparator.LESS_OR_EQUAL;
		}
		else if(node instanceof GreaterNode)
		{
			comparator = Comparator.GREATER;
		}
		else if(node instanceof GreaterOrEqualNode)
		{
			comparator = Comparator.GREATER_OR_EQUAL;
		}
		else
		{
			throw new RuntimeException("Unknown node: " + node);
		}
			
	}
	
	@Override
	protected boolean check(ErrorHandler errors, Object left, Object right)
	{
		Number lv = castLeftNotNull(errors, left);
		Number rv = castLeftNotNull(errors, right);
		
		return comparator.check(lv, rv);
	}

}
