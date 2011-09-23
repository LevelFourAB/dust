package se.l4.dust.core.internal.expression.ast;

/**
 * Node for an operation that checks if the left side is less than the right
 * side.
 * 
 * @author Andreas Holstenson
 *
 */
public class LessNode
	extends LeftRightNode
{

	public LessNode(Node left, Node right)
	{
		super(left, right);
	}

}
