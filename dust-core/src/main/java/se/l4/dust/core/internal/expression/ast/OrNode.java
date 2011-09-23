package se.l4.dust.core.internal.expression.ast;

/**
 * Node for a logical or operation.
 * 
 * @author Andreas Holstenson
 *
 */
public class OrNode
	extends LeftRightNode
{
	public OrNode(Node left, Node right)
	{
		super(left, right);
	}
}
