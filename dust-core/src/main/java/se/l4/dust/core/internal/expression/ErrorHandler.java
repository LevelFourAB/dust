package se.l4.dust.core.internal.expression;

import se.l4.dust.api.expression.ExpressionException;
import se.l4.dust.core.internal.expression.ast.Node;

public interface ErrorHandler
{

	/**
	 * Report a fatal error.
	 *
	 * @param node
	 * @param message
	 * @return
	 */
	ExpressionException error(Node node, String message);

	/**
	 * Report a fatal error.
	 *
	 * @param node
	 * @param message
	 * @return
	 */
	ExpressionException error(Node node, String message, Throwable cause);

	/**
	 * Report a fatal error.
	 *
	 * @param node
	 * @param message
	 * @return
	 */
	ExpressionException error(Node node, Throwable cause);

}
