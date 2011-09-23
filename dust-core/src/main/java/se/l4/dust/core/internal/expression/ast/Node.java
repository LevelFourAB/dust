package se.l4.dust.core.internal.expression.ast;

/**
 * Node within the AST.
 * 
 * @author Andreas Holstenson
 *
 */
public interface Node
{
	/**
	 * Get the line where the node originated.
	 * 
	 * @return
	 */
	int getLine();
	
	/**
	 * Get at which position in the line the node originated.
	 * 
	 * @return
	 */
	int getPositionInLine();
}
