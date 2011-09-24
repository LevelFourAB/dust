package se.l4.dust.core.internal.expression.resolver;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import se.l4.crayon.Crayon;
import se.l4.dust.api.Context;
import se.l4.dust.api.conversion.TypeConverter;
import se.l4.dust.api.expression.DynamicMethod;
import se.l4.dust.api.expression.DynamicProperty;
import se.l4.dust.api.expression.ExpressionEncounter;
import se.l4.dust.api.expression.ExpressionSource;
import se.l4.dust.core.internal.conversion.ConversionModule;
import se.l4.dust.core.internal.expression.ErrorHandler;
import se.l4.dust.core.internal.expression.ErrorHandlerImpl;
import se.l4.dust.core.internal.expression.ExpressionParser;
import se.l4.dust.core.internal.expression.ExpressionResolver;
import se.l4.dust.core.internal.expression.ExpressionsImpl;
import se.l4.dust.core.internal.expression.ast.Node;
import se.l4.dust.core.internal.expression.invoke.ChainInvoker;
import se.l4.dust.core.internal.expression.invoke.ConstantInvoker;
import se.l4.dust.core.internal.expression.invoke.DynamicPropertyInvoker;
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
	private ExpressionsImpl expressions;
	private Map<String, String> namespaces;

	@Before
	public void before()
	{
		Injector injector = Guice.createInjector(new ConversionModule());
		injector.getInstance(Crayon.class).start();
		tc = injector.getInstance(TypeConverter.class);
		expressions = new ExpressionsImpl(tc);
		expressions.addSource("dust:test", new TestSource());
		
		namespaces = new HashMap<String, String>();
		namespaces.put("t", "dust:test");
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
	
	@Test
	public void testCommonProperty()
		throws Exception
	{
		test("t:emit", Person.class, new DynamicPropertyInvoker(null, new Property("emit")));
	}
	
	private void test(String expr, Class<?> context, Invoker expectedResult)
	{
		ErrorHandler errors = new ErrorHandlerImpl(expr);
		Node node = ExpressionParser.parse(expr);
		Invoker invoker = new ExpressionResolver(
				tc, 
				expressions,
				namespaces,
				errors, 
				node
			).resolve(context);
		
		Assert.assertEquals("Resolved result does not match", expectedResult, invoker);
	}
	
	private static class TestSource
		implements ExpressionSource
	{

		@Override
		public DynamicProperty getProperty(ExpressionEncounter encounter, String name)
		{
			return new Property(name);
		}

		@Override
		public DynamicMethod getMethod(ExpressionEncounter encounter,
				String name, Class... parameters)
		{
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	private static class Property
		implements DynamicProperty
	{
		private final String name;

		public Property(String name)
		{
			this.name = name;
		}

		@Override
		public Object getValue(Context context, Object root)
		{
			return 12;
		}

		@Override
		public Class<?> getType()
		{
			return Integer.class;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if(this == obj)
				return true;
			if(obj == null)
				return false;
			if(getClass() != obj.getClass())
				return false;
			Property other = (Property) obj;
			if(name == null)
			{
				if(other.name != null)
					return false;
			}
			else if(!name.equals(other.name))
				return false;
			return true;
		}
	}
}
