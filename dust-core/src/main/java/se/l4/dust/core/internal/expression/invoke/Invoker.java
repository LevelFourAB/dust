package se.l4.dust.core.internal.expression.invoke;

import se.l4.dust.api.Context;
import se.l4.dust.core.internal.expression.ErrorHandler;
import se.l4.dust.core.internal.expression.ExpressionCompiler;
import se.l4.dust.core.internal.expression.ast.Node;

import com.fasterxml.classmate.ResolvedType;

/**
 * Invoker within an expression tree.
 * 
 * @author Andreas Holstenson
 *
 */
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
	 * @param context
	 * @param root
	 * @param instance
	 * @return
	 */
	Object get(ErrorHandler errors, Context context, Object root, Object instance);
	
	/**
	 * Check if this invoker supports getting values.
	 * 
	 * @return
	 */
	boolean supportsGet();
	
	/**
	 * Set the value of this invoker. Not all invokers support setting values.
	 * 
	 * @param errors
	 * @param context
	 * @param root
	 * @param instance
	 * @param value
	 */
	void set(ErrorHandler errors, Context context, Object root, Object instance, Object value);
	
	/**
	 * Check if this invoker supports setting values.
	 * 
	 * @return
	 */
	boolean supportsSet();
	
	/**
	 * Get the node of the invoker.
	 * 
	 * @return
	 */
	Node getNode();

	/**
	 * Get this invoker as a Java expression that can be compiled with 
	 * Javassist.
	 * 
	 * @param errors
	 * @param compiler
	 * @param context
	 * 		context as a Java expression. Can be chained.
	 * @return
	 */
	String toJavaGetter(ErrorHandler errors, ExpressionCompiler compiler, String context);
	
	/**
	 * Get this invoker as Java expression that can be compiled with Javassist.
	 * 
	 * @param errors
	 * @param compiler
	 * @param context
	 * @return
	 */
	String toJavaSetter(ErrorHandler errors, ExpressionCompiler compiler, String context);
}
