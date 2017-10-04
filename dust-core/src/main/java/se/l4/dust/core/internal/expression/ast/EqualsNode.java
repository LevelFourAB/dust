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
	public EqualsNode(int line, int position, Node left, Node right)
	{
		super(line, position, left, right);
	}
}
