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

	public LessNode(int line, int position, Node left, Node right)
	{
		super(line, position, left, right);
	}

}
