package se.l4.dust.core.internal.expression.ast;

/**
 * Node for an operation that adds the right side to the left.
 * 
 * @author Andreas Holstenson
 *
 */
public class AddNode
	extends LeftRightNode
{

	public AddNode(int line, int position, Node left, Node right)
	{
		super(line, position, left, right);
	}

}
