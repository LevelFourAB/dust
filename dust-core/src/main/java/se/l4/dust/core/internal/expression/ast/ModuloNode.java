package se.l4.dust.core.internal.expression.ast;

/**
 * Node for a modulo operation between the left and right side.
 * 
 * @author Andreas Holstenson
 *
 */
public class ModuloNode
	extends LeftRightNode
{

	public ModuloNode(Node left, Node right)
	{
		super(left, right);
	}

}
