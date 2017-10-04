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
	public ChainNode(int line, int position, Node left, Node right)
	{
		super(line, position, left, right);
	}
}
