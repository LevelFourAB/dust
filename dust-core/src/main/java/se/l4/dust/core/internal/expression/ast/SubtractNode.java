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

	public SubtractNode(int line, int position, Node left, Node right)
	{
		super(line, position, left, right);
	}

}
