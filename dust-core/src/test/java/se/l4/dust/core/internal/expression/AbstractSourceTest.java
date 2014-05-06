package se.l4.dust.core.internal.expression;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;

import se.l4.crayon.Crayon;
import se.l4.dust.api.conversion.TypeConverter;
import se.l4.dust.api.expression.Expression;
import se.l4.dust.api.expression.ExpressionSource;
import se.l4.dust.core.internal.conversion.ConversionModule;
import se.l4.dust.core.internal.expression.ast.Node;
import se.l4.dust.core.internal.expression.invoke.Invoker;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;

/**
 * Abstract test class for {@link ExpressionSource}.
 * 
 * @author Andreas Holstenson
 *
 */
public abstract class AbstractSourceTest
{
	private TypeConverter tc;
	private ExpressionsImpl expressions;
	private Map<String, String> namespaces;
	
	@Before
	public void before()
	{
		Injector injector = Guice.createInjector(new ConversionModule());
		injector.getInstance(Crayon.class).start();
		tc = injector.getInstance(TypeConverter.class);
		
		expressions = new ExpressionsImpl(tc, Stage.DEVELOPMENT);
		expressions.addSource("dust:test", createSource());
		
		namespaces = new HashMap<String, String>();
		namespaces.put("t", "dust:test");
	}
	
	protected abstract ExpressionSource createSource();

	protected Object execute(String expr, Object in)
	{
		if(in == null) throw new NullPointerException("in must not be null");
		
		return execute(expr, in.getClass(), in);
	}
	
	protected Expression compile(String expr, Class<?> context)
	{
//		ExpressionDebugger debugger = new ExpressionDebugger(
//				tc, 
//				expressions,
//				namespaces,
//				expr, 
//				context
//			);
		
		ErrorHandler errors = new ErrorHandlerImpl(expr);
		Node node = ExpressionParser.parse(expr);
		Invoker invoker = new ExpressionResolver(
				tc, 
				expressions,
				null,
				namespaces,
				errors, 
				node
			).resolve(context);
		
		ExpressionCompiler compiler = new ExpressionCompiler(errors, expr, context, invoker);
		return compiler.compile();
	}
	
	protected Object execute(String expr, Class<?> context, Object in)
	{
		Expression compiled = compile(expr, context);
		return compiled.get(null, in);
	}
}
