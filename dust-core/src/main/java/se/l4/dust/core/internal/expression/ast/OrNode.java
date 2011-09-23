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
	public OrNode(int line, int position, Node left, Node right)
	{
		super(line, position, left, right);
	}
}
