package se.l4.dust.core.internal.expression.invoke;

import se.l4.dust.core.internal.expression.ErrorHandler;
import se.l4.dust.core.internal.expression.ast.Node;

import com.fasterxml.classmate.ResolvedType;

public interface Invoker
{
	/**
	 * Get the return class of the invocation, for the generic type use
	 * {@link #getReturnType()}.
	 * 
	 * @return
	 */
	Class<?> getReturnClass();
	
	/**
	 * Get the generic return type of the invocation. This may return
	 * {@code null} if no generic type is available.
	 * 
	 * @return
	 */
	ResolvedType getReturnType();
	
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
