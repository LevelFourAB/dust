package se.l4.dust.core.internal.expression.ast;

/**
 * Abstract node implementation, contains shared information such as position
 * information.
 * 
 * @author Andreas Holstenson
 *
 */
public class AbstractNode
	implements Node
{
	private final int line;
	private final int position;

	public AbstractNode(int line, int position)
	{
		this.line = line;
		this.position = position;
	}
	
	@Override
	public int getLine()
	{
		return line;
	}
	
	@Override
	public int getPositionInLine()
	{
		return position;
	}
}
