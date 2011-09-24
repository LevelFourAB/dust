package se.l4.dust.core.internal.expression.invoke;

import se.l4.dust.core.internal.expression.ErrorHandler;
import se.l4.dust.core.internal.expression.ast.Node;

public interface Invoker
{
	/**
	 * Get the return type of the invocation.
	 * 
	 * @return
	 */
	Class<?> getResult();
	
	/**
	 * Invoke in interpreted mode.
	 * @param root TODO
	 * @param instance
	 * 
	 * @return
	 */
	Object interpret(ErrorHandler errors, Object root, Object instance);
	
	/**
	 * Set the value of this invoker. Not all invokers support setting values.
	 * 
	 * @param errors
	 * @param root
	 * @param instance
	 * @param value
	 */
	void set(ErrorHandler errors, Object root, Object instance, Object value);
	
	/**
	 * Get the node of the invoker.
	 * 
	 * @return
	 */
	Node getNode();
}
