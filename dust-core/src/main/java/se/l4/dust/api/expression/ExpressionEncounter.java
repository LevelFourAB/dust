package se.l4.dust.api.expression;

import se.l4.dust.api.resource.ResourceLocation;


/**
 * Encounter information for an expression encounter.
 *
 * @author Andreas Holstenson
 *
 */
public interface ExpressionEncounter
{
	/**
	 * Get the source of this expression.
	 *
	 * @return
	 */
	ResourceLocation getSource();

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
