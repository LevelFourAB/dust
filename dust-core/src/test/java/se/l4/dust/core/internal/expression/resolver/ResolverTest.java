package se.l4.dust.core.internal.expression.resolver;

import java.lang.reflect.Method;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import se.l4.crayon.Crayon;
import se.l4.dust.api.conversion.TypeConverter;
import se.l4.dust.core.internal.conversion.ConversionModule;
import se.l4.dust.core.internal.expression.ErrorHandler;
import se.l4.dust.core.internal.expression.ExpressionParser;
import se.l4.dust.core.internal.expression.ExpressionResolver;
import se.l4.dust.core.internal.expression.ast.Node;
import se.l4.dust.core.internal.expression.invoke.ChainInvoker;
import se.l4.dust.core.internal.expression.invoke.ConstantInvoker;
import se.l4.dust.core.internal.expression.invoke.Invoker;
import se.l4.dust.core.internal.expression.invoke.MethodInvoker;
import se.l4.dust.core.internal.expression.invoke.MethodPropertyInvoker;
import se.l4.dust.core.internal.expression.invoke.ThisInvoker;
import se.l4.dust.core.internal.expression.model.Person;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Expression resolution tests. Tests that the resolver will handle expressions
 * in the correct way.
 * 
 * @author Andreas Holstenson
 *
 */
public class ResolverTest
{
	private TypeConverter tc;

	@Before
	public void before()
	{
		Injector injector = Guice.createInjector(new ConversionModule());
		injector.getInstance(Crayon.class).start();
		tc = injector.getInstance(TypeConverter.class);
	}
	
	@Test
	public void testProperty()
		throws Exception
	{
		Method m = Person.class.getMethod("getName");
		test("name", Person.class, new MethodPropertyInvoker(null, m));
	}
	
	@Test
	public void testLong()
		throws Exception
	{
		test("12", Person.class, new ConstantInvoker(null, 12l));
	}
	
	@Test
	public void testDouble()
		throws Exception
	{
		test("12.0", Person.class, new ConstantInvoker(null, 12.0));
	}
	
	@Test
	public void testString()
		throws Exception
	{
		test("'string'", Person.class, new ConstantInvoker(null, "string"));
	}
	
	@Test
	public void testKeywords()
		throws Exception
	{
		test("this", Person.class, new ThisInvoker(null, Person.class));
		test("false", Person.class, new ConstantInvoker(null, false));
		test("true", Person.class, new ConstantInvoker(null, true));
		test("null", Person.class, new ConstantInvoker(null, null));
	}
	
	@Test
	public void testSimpleChain()
		throws Exception
	{
		Method m1 = Person.class.getMethod("getName");
		Method m2 = String.class.getMethod("getClass");
		test("name.class", Person.class, new ChainInvoker(null, 
			new MethodPropertyInvoker(null, m1),
			new MethodPropertyInvoker(null, m2)
		));
	}
	
	@Test
	public void testMethod()
		throws Exception
	{
		Method m1 = Person.class.getMethod("getName");
		test("getName()", Person.class, new MethodInvoker(null,
			m1,
			new Invoker[0]
		));
	}
	
	@Test
	public void testMethodChain()
		throws Exception
	{
		Method m1 = Person.class.getMethod("getName");
		Method m2 = String.class.getMethod("getClass");
		test("getName().class", Person.class, new ChainInvoker(null,
			new MethodInvoker(null,
				m1,
				new Invoker[0]
			),
			new MethodPropertyInvoker(null, m2)
		));
	}
	
	private void test(String expr, Class<?> context, Invoker expectedResult)
	{
		ErrorHandler errors = new ErrorHandler(expr);
		Node node = ExpressionParser.parse(expr);
		Invoker invoker = new ExpressionResolver(tc, errors, node)
			.resolve(context);
		
		Assert.assertEquals("Resolved result does not match", expectedResult, invoker);
	}
}
