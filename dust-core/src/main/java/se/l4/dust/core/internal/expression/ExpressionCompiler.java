package se.l4.dust.core.internal.expression;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;

import com.google.common.primitives.Primitives;

import se.l4.dust.api.expression.Expression;
import se.l4.dust.api.expression.ExpressionException;
import se.l4.dust.core.internal.expression.invoke.Invoker;

/**
 * Compiler for expressions. Will take a tree of {@link Invoker}s and compile
 * them into a {@link Expression} class.
 * 
 * @author Andreas Holstenson
 *
 */
public class ExpressionCompiler
{
	private static final AtomicInteger compiled = new AtomicInteger();
	private final ErrorHandler errors;
	private final Invoker root;
	
	private final List<DeclaredItem> items;
	private final Class<?> context;
	private final String rootContext;
	
	public ExpressionCompiler(ErrorHandler errors, Class<?> context, Invoker root)
	{
		this.errors = errors;
		this.context = context;
		this.root = root;
		
		rootContext = "((" + context.getName() + ") $2)";
		
		items = new ArrayList<DeclaredItem>();
	}
	
	public Expression compile()
	{
		try
		{
			ClassPool pool = ClassPool.getDefault();
			CtClass exprIf = pool.get(Expression.class.getName());
			CtMethod get = exprIf.getMethod("get", "(Lse/l4/dust/api/Context;Ljava/lang/Object;)Ljava/lang/Object;");
			CtMethod set = exprIf.getMethod("set", "(Lse/l4/dust/api/Context;Ljava/lang/Object;Ljava/lang/Object;)V");
			
			CtClass type = pool.makeClass("se.l4.dust.core.internal.expression.Expression$$" + compiled.incrementAndGet());
			type.addInterface(exprIf);
			
			// Create the expression
			String javaGetter = root.toJavaGetter(errors, this, rootContext);
			String javaSetter = root.toJavaSetter(errors, this, rootContext);
			
			// Create constructor information
			Class[] typed = new Class[items.size()];
			CtClass[] ctTyped = new CtClass[items.size()];
			Object[] values = new Object[items.size()];
			StringBuilder body = new StringBuilder("");
			
			int i = 0;
			for(DeclaredItem item : items)
			{
				typed[i] = item.type;
				ctTyped[i] = pool.getCtClass(item.type.getName());
				values[i] = item.instance;
				
				// Add field to constructed class
				CtField field = new CtField(ctTyped[i], item.name, type);
				type.addField(field);
				
				body.append("this.")
					.append(item.name)
					.append(" = $")
					.append((i+1))
					.append(";");
				
				i++;
			}
			
			CtConstructor constructor = CtNewConstructor.make(ctTyped, new CtClass[0], type);
			if(body.length() > 0)
			{
				constructor.setBody("{" + body.toString() + "}");
			}
			
			type.addConstructor(constructor);
			
			// Create the get method
			CtMethod impl = CtNewMethod.copy(get, type, null);
			
			String fullExpr = "return "	+ wrap(root.getReturnClass(), javaGetter) + ";";
			impl.setBody(fullExpr);
			type.addMethod(impl);
			
			impl = CtNewMethod.copy(set, type, null);
			if(javaSetter != null)
			{
				impl.setBody(javaSetter + ";");
			}
			else
			{
				impl.setBody("throw new " + ExpressionException.class.getName() + "(null, 0, 0, \"Setter not supported\");");
			}
			type.addMethod(impl);
			
			Class<? extends Expression> c = type.toClass();
			
			Constructor<? extends Expression> ct = c.getConstructor(typed);
			return ct.newInstance(values);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw errors.error(root.getNode(), "Compilation failed; " + e.getMessage(), e);
		}
	}

	/**
	 * For invoker usage: Add an input to this instance.
	 * 
	 * @param <T>
	 * @param input
	 * @param instance
	 * @return
	 */
	public <T> String addInput(Class<T> input, T instance)
	{
		DeclaredItem item = new DeclaredItem("input" + items.size(), input, instance);
		items.add(item);
		return "this." + item.name;
	}
	
	/**
	 * Create a cast (including parenthesizes).
	 * 
	 * @param result
	 * @return
	 */
	public String cast(Class<?> result)
	{
		return "(" + castNoParens(result) + ")";
	}
	
	private String castNoParens(Class<?> result)
	{
		if(result.isArray())
		{
			return castNoParens(result.getComponentType()) + "[]";
		}
		else
		{
			return result.getName();
		}
	}

	/**
	 * Potentially wrap the given input.
	 * 
	 * @param type
	 * @param input
	 * @return
	 */
	public String wrap(Class<?> type, String input)
	{
		if(! type.isPrimitive() || type == void.class) return input;
		
		return Primitives.wrap(type).getName() + ".valueOf(" + input + ")";
	}
	
	/**
	 * Potentially unwrap the given input.
	 * 
	 * @param type
	 * @param input
	 * @return
	 */
	public String unwrap(Class<?> type, String input)
	{
		if(type.isPrimitive()) return input;
		
		type = Primitives.unwrap(type);
		if(type.isPrimitive())
		{
			return input + "." + type.getSimpleName() + "Value()";
		}
		
		// Nothing we can do, no primitive available
		return input;
	}
	
	public String getRootContext()
	{
		return rootContext;
	}
	
	private static class DeclaredItem
	{
		private final String name;
		private final Class<?> type;
		private final Object instance;

		public DeclaredItem(String name, Class<?> type, Object instance)
		{
			this.name = name;
			this.type = type;
			this.instance = instance;
		}
	}

}
