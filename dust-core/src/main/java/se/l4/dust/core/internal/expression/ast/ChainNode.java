package se.l4.dust.core.internal.expression.ast;

/**
 * Node for a chain of identifier or method calls.
 * 
 * @author Andreas Holstenson
 *
 */
public class ChainNode
	extends LeftRightNode
{
	public ChainNode(Node left, Node right)
	{
		super(left, right);
	}
}
