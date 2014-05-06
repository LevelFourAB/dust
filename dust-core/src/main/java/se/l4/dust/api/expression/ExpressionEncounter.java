package se.l4.dust.api.expression;

import java.net.URL;


/**
 * Encounter information for an expression encounter.
 * 
 * @author Andreas Holstenson
 *
 */
public interface ExpressionEncounter
{
	URL getSource();
	
	/**
	 * Get if this is the root context.
	 * 
	 * @return
	 */
	boolean isRoot();
	
	/**
	 * Get the current context.
	 * 
	 * @return
	 */
	Class<?> getContext();
	
	/**
	 * Get the root context.
	 * 
	 * @return
	 */
	Class<?> getRoot();
	
	/**
	 * Raise an error that has occurred during this encounter. Example:
	 * 
	 * <pre>
	 * throw encounter.error("Invalid content");
	 * </pre>
	 * 
	 * @param message
	 * @return
	 */
	ExpressionException error(String message);
}
