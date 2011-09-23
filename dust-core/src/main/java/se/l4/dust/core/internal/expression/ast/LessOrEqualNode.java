package se.l4.dust.core.internal.expression.ast;

/**
 * Node for an operation that checks if the left side is less or equal to the
 * right side.
 * 
 * @author Andreas Holstenson
 *
 */
public class LessOrEqualNode
	extends LeftRightNode
{

	public LessOrEqualNode(int line, int position, Node left, Node right)
	{
		super(line, position, left, right);
	}

}
