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

	public ModuloNode(int line, int position, Node left, Node right)
	{
		super(line, position, left, right);
	}

}
