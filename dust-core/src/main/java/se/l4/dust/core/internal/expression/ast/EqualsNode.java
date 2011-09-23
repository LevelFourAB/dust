package se.l4.dust.core.internal.expression.ast;

/**
 * Node for an equals operation between a left and right part.
 * 
 * @author Andreas Holstenson
 *
 */
public class EqualsNode
	extends LeftRightNode
{
	public EqualsNode(Node left, Node right)
	{
		super(left, right);
	}
}
