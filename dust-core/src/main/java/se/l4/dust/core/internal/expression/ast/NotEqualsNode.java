package se.l4.dust.core.internal.expression.ast;

/**
 * Node for an operation that checks that the left side is not equal to the
 * right.
 * 
 * @author Andreas Holstenson
 *
 */
public class NotEqualsNode
	extends LeftRightNode
{
	public NotEqualsNode(int line, int position, Node left, Node right)
	{
		super(line, position, left, right);
	}
}
