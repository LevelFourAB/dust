package se.l4.dust.core.internal.expression.ast;

/**
 * Node for an operation that checks if the left side is greater than or equal
 * to the right one.
 * 
 * @author Andreas Holstenson
 *
 */
public class GreaterOrEqualNode
	extends LeftRightNode
{

	public GreaterOrEqualNode(int line, int position, Node left, Node right)
	{
		super(line, position, left, right);
	}

}
