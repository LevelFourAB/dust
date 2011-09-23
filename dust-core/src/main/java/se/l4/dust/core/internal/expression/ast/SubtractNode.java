package se.l4.dust.core.internal.expression.ast;

/**
 * Node for a subtract operation where the left side is subtracted with the
 * right side.
 * 
 * @author Andreas Holstenson
 *
 */
public class SubtractNode
	extends LeftRightNode
{

	public SubtractNode(Node left, Node right)
	{
		super(left, right);
	}

}
