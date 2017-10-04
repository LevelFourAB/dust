package se.l4.dust.core.internal.expression.resolver;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;

import junit.framework.Assert;
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
import se.l4.dust.core.internal.expression.invoke.ArrayInvoker;
import se.l4.dust.core.internal.expression.invoke.ChainInvoker;
import se.l4.dust.core.internal.expression.invoke.ConstantInvoker;
import se.l4.dust.core.internal.expression.invoke.DynamicMethodInvoker;
import se.l4.dust.core.internal.expression.invoke.DynamicPropertyInvoker;
import se.l4.dust.core.internal.expression.invoke.FieldPropertyInvoker;
import se.l4.dust.core.internal.expression.invoke.Invoker;
import se.l4.dust.core.internal.expression.invoke.MethodInvoker;
import se.l4.dust.core.internal.expression.invoke.MethodPropertyInvoker;
import se.l4.dust.core.internal.expression.invoke.ThisInvoker;
import se.l4.dust.core.internal.expression.model.Person;

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
		expressions = new ExpressionsImpl(tc, Stage.DEVELOPMENT);
		expressions.addSource("dust:test", new TestSource());

		namespaces = new HashMap<>();
		namespaces.put("t", "dust:test");
	}

	@Test
	public void testProperty()
		throws Exception
	{
		Method m = Person.class.getMethod("getName");
		test("name", Person.class, new MethodPropertyInvoker(null, null, m, null));
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
			new MethodPropertyInvoker(null, null, m1, null),
			new MethodPropertyInvoker(null, null, m2, null)
		));
	}

	@Test
	public void testMethod()
		throws Exception
	{
		Method m1 = Person.class.getMethod("getName");
		test("getName()", Person.class, new MethodInvoker(null,
			null,
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
				null,
				m1,
				new Invoker[0]
			),
			new MethodPropertyInvoker(null, null, m2, null)
		));
	}

	@Test
	public void testCommonProperty()
		throws Exception
	{
		test("t:emit", Person.class, new DynamicPropertyInvoker(null, new Property("emit")));
	}

	@Test
	public void testCommonPropertyWithSubResoultion()
		throws Exception
	{
		test("t:emit.sub", Person.class, new ChainInvoker(
			null,
			new DynamicPropertyInvoker(null, new Property("emit")),
			new DynamicPropertyInvoker(null, new Property("sub"))
		));
	}

	@Test
	public void testCommonPropertyWithMethodResoultion()
		throws Exception
	{
		test("t:emit.format('kaka')", Person.class, new ChainInvoker(
			null,
			new DynamicPropertyInvoker(null, new Property("emit")),
			new DynamicMethodInvoker(null, new TestMethod("format"), Person.class, new Invoker[] {
				new ConstantInvoker(null, "kaka")
			})
		));
	}

	@Test
	public void testCommonPropertyWithSubAndMethodResoultion()
		throws Exception
	{
		test("t:emit.sub.format('kaka')", Person.class, new ChainInvoker(
			null,
			new DynamicPropertyInvoker(null, new Property("emit")),
			new ChainInvoker(
				null,
				new DynamicPropertyInvoker(null, new Property("sub")),
				new DynamicMethodInvoker(null, new TestMethod("format"), Person.class, new Invoker[] {
					new ConstantInvoker(null, "kaka")
				})
			)
		));
	}

	@Test
	public void testGenericMethod()
	{
		resolve("get('red').bytes", TestMap.class);
	}

	@Test
	public void testIndexedMethod()
	{
		resolve("map['string']", IndexContainer.class);
	}

	@Test
	public void testMethodNullParam()
		throws Exception
	{
		Method m1 = Person.class.getMethod("getSuffixedName", String.class);
		test("getSuffixedName(null)", Person.class, new MethodInvoker(null,
			null,
			m1,
			new Invoker[] { new ConstantInvoker(null, null) }
		));
	}

	@Test
	public void testMethodConstantParam()
		throws Exception
	{
		Method m1 = Person.class.getMethod("getSuffixedName", String.class);
		test("getSuffixedName('value')", Person.class, new MethodInvoker(null,
			null,
			m1,
			new Invoker[] { new ConstantInvoker(null, "value") }
		));
	}

	@Test
	public void testExposedProperty()
		throws Exception
	{
		Field f = Person.class.getDeclaredField("verified");
		test("verified", Person.class, new FieldPropertyInvoker(null, null, f));
	}

	@Test
	public void testLongArray()
		throws Exception
	{
		test("[ 1, 2 ]", Person.class, new ArrayInvoker(null, long.class, new Invoker[] {
			new ConstantInvoker(null, 1l),
			new ConstantInvoker(null, 2l),
		}));
	}

	@Test
	public void testStringArray()
		throws Exception
	{
		test("[ '1', '' ]", Person.class, new ArrayInvoker(null, String.class, new Invoker[] {
			new ConstantInvoker(null, "1"),
			new ConstantInvoker(null, ""),
		}));
	}

	@Test
	public void testArrayWithProperty()
		throws Exception
	{
		Method m = Person.class.getMethod("getName");
		test("[ name ]", Person.class, new ArrayInvoker(null, String.class, new Invoker[] {
			new MethodPropertyInvoker(null, null, m, null)
		}));
	}

	public static class IndexContainer
	{
		public Map<String, String> getMap()
		{
			return null;
		}
	}

	public static class TestMap
		extends HashMap<String, String>
	{
	}

	private void test(String expr, Class<?> context, Invoker expectedResult)
	{
		Invoker invoker = resolve(expr, context);

		Assert.assertEquals("Resolved result does not match", expectedResult, invoker);
	}

	private Invoker resolve(String expr, Class<?> context)
	{
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
		return invoker;
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
		public DynamicMethod getMethod(ExpressionEncounter encounter, String name, Class... parameters)
		{
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
		public Object get(Context context, Object root)
		{
			return 12;
		}

		@Override
		public boolean supportsGet()
		{
			return true;
		}

		@Override
		public void set(Context context, Object root, Object value)
		{
		}

		@Override
		public boolean supportsSet()
		{
			return false;
		}

		@Override
		public Class<?> getType()
		{
			return Integer.class;
		}

		@Override
		public boolean needsContext()
		{
			return true;
		}

		@Override
		public DynamicProperty getProperty(ExpressionEncounter encounter, String name)
		{
			if("sub".equals(name))
			{
				return new Property(name);
			}

			return null;
		}

		@Override
		public DynamicMethod getMethod(ExpressionEncounter encounter, String name, Class... parameters)
		{
			if("format".equals(name))
			{
				return new TestMethod(name);
			}

			return null;
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

		@Override
		public String toString()
		{
			return "TestProperty{name=" + name + "}";
		}
	}

	private static class TestMethod
		implements DynamicMethod
	{
		private final String name;

		public TestMethod(String name)
		{
			this.name = name;
		}

		@Override
		public Object invoke(Context context, Object instance, Object... parameters)
		{
			return null;
		}

		@Override
		public Class<?> getType()
		{
			return String.class;
		}

		@Override
		public Class<?>[] getParametersType()
		{
			return new Class[] { String.class };
		}

		@Override
		public boolean needsContext()
		{
			return true;
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
			TestMethod other = (TestMethod) obj;
			if(name == null)
			{
				if(other.name != null)
					return false;
			}
			else if(!name.equals(other.name))
				return false;
			return true;
		}

		@Override
		public String toString()
		{
			return "TestMethod{name=" + name + "}";
		}
	}
}
