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
	public NotEqualsNode(Node left, Node right)
	{
		super(left, right);
	}
}
