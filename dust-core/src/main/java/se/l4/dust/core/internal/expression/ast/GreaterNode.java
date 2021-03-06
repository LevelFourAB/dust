package se.l4.dust.core.internal.expression.ast;

/**
 * Node for an operation that checks if the left side is greater than the
 * right side.
 *
 * @author Andreas Holstenson
 *
 */
public class GreaterNode
	extends LeftRightNode
{

	public GreaterNode(int line, int position, Node left, Node right)
	{
		super(line, position, left, right);
	}

}
