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

	public AddNode(Node left, Node right)
	{
		super(left, right);
	}

}
