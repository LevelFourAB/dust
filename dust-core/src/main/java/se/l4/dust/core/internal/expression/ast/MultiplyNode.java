package se.l4.dust.core.internal.expression.ast;

/**
 * Node for a multiplication between the left and right side.
 * 
 * @author Andreas Holstenson
 *
 */
public class MultiplyNode
	extends LeftRightNode
{

	public MultiplyNode(Node left, Node right)
	{
		super(left, right);
	}

}
