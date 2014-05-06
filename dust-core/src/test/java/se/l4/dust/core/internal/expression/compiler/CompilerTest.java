package se.l4.dust.core.internal.expression.compiler;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import se.l4.crayon.Crayon;
import se.l4.dust.api.conversion.TypeConverter;
import se.l4.dust.api.expression.Expression;
import se.l4.dust.core.internal.conversion.ConversionModule;
import se.l4.dust.core.internal.expression.ErrorHandler;
import se.l4.dust.core.internal.expression.ErrorHandlerImpl;
import se.l4.dust.core.internal.expression.ExpressionCompiler;
import se.l4.dust.core.internal.expression.ExpressionParser;
import se.l4.dust.core.internal.expression.ExpressionResolver;
import se.l4.dust.core.internal.expression.ExpressionsImpl;
import se.l4.dust.core.internal.expression.ast.Node;
import se.l4.dust.core.internal.expression.invoke.Invoker;
import se.l4.dust.core.internal.expression.model.Person;
import se.l4.dust.core.internal.expression.resolver.DebuggerTest.IndexContainer;
import se.l4.dust.core.internal.expression.resolver.DebuggerTest.TestMap;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;

/**
 * Test for compiled expressions.
 * 
 * @author Andreas Holstenson
 *
 */
public class CompilerTest
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
		namespaces = new HashMap<String, String>();
	}
	
	@Test
	public void testPersonName()
	{
		Person p = new Person();
		p.setName("Name");
		Object o = compileAndRun("name", p);
		
		Assert.assertEquals("Name", o);
	}
	
	@Test
	public void testPersonClass()
	{
		Person p = new Person();
		p.setName("Name");
		Object o = compileAndRun("name.class", p);
		
		Assert.assertEquals(String.class, o);
	}
	
	@Test
	public void testEquals()
	{
		Person p = new Person();
		p.setName("Name");
		Object o = compileAndRun("name == 'Name'", p);
		
		Assert.assertEquals(true, o);
	}
	
	@Test
	public void testNotEquals()
	{
		Person p = new Person();
		p.setName("Name");
		Object o = compileAndRun("name != 'Name'", p);
		
		Assert.assertEquals(false, o);
	}
	
	@Test
	public void testNotEqualsNull()
	{
		Person p = new Person();
		p.setName("Name");
		Object o = compileAndRun("name != null", p);
		
		Assert.assertEquals(true, o);
	}
	
	@Test
	public void testExposedMethod()
	{
		Person p = new Person();
		Object o = compileAndRun("verified", p);
		
		Assert.assertEquals(false, o);
	}
	
	@Test
	public void testAnd()
	{
		Person p = new Person();
		p.setName("Name");
		Object o = compileAndRun("true && false", p);
		Assert.assertEquals(false, o);
		
		o = compileAndRun("true && name == 'Name'", p);
		Assert.assertEquals(true, o);
		
		o = compileAndRun("name != null && name == 'Name'", p);
		Assert.assertEquals(true, o);
	}
	
	@Test
	public void testConversion()
	{
		Person p = new Person();
		p.setName("true");
		Object o = compileAndRun("name && verified", p);
		
		Assert.assertEquals(false, o);
	}
	
	@Test
	public void testStringConcat()
	{
		Person p = new Person();
		p.setName("test");
		Object o = compileAndRun("name + 'test'", p);
		
		Assert.assertEquals("testtest", o);
	}
	
	@Test
	public void testTernary()
	{
		Person p = new Person();
		p.setName("test");
		Object o = compileAndRun("true ? name : 'string'", p);
		Assert.assertEquals("test", o);
		
		o = compileAndRun("verified ? name : 'string'", p);
		Assert.assertEquals("string", o);
	}
	
	@Test
	public void testNumericComparison()
	{
		Person p = new Person();
		p.setAge(19);
		Object o = compileAndRun("age < 19", p);
		Assert.assertEquals(false, o);
		
		o = compileAndRun("age <= 19", p);
		Assert.assertEquals(true, o);
		
		o = compileAndRun("age > 19", p);
		Assert.assertEquals(false, o);
		
		o = compileAndRun("age >= 8", p);
		Assert.assertEquals(true, o);
		
		o = compileAndRun("age == 19", p);
		Assert.assertEquals(true, o);
		
		o = compileAndRun("age != 18", p);
		Assert.assertEquals(true, o);
	}
	
	@Test
	public void testNumericOperations()
	{
		Person p = new Person();
		p.setAge(19);
		Object o = compileAndRun("age + 1", p);
		Assert.assertEquals(20l, o);
		
		o = compileAndRun("age - 2", p);
		Assert.assertEquals(17l, o);
		
		o = compileAndRun("age / 2", p);
		Assert.assertEquals(9l, o);
		
		o = compileAndRun("age * 2", p);
		Assert.assertEquals(38l, o);
		
		o = compileAndRun("age % 10", p);
		Assert.assertEquals(9l, o);
	}
	
	@Test
	public void testGetClass()
	{
		Object o = compileAndRun("class", "string");
		
		Assert.assertEquals(String.class, o);
	}
	
	@Test
	public void testNullPersonName()
	{
		Person p = new Person();
		Object o = compileAndRun("name", p);
		
		Assert.assertEquals(null, o);
	}
	
	@Test
	public void testPersonNameLength()
	{
		Person p = new Person();
		p.setName("Name");
		Object o = compileAndRun("name.length()", p);
		
		Assert.assertEquals(4, o);
	}
	
	@Test
	public void testPersonNameIs()
	{
		Person p = new Person();
		p.setName("One");
		
		Object o = compileAndRun("name == 'One'", p);
		
		Assert.assertEquals(true, o);
		
		o = compileAndRun("name != 'One'", p);
		Assert.assertEquals(false, o);
	}
	
	@Test
	public void testPersonAge()
	{
		Person p = new Person();
		p.setAge(22);
		
		Object o = compileAndRun("age == 22", p);
		Assert.assertEquals(true, o);
		
		o = compileAndRun("age != 23", p);
		Assert.assertEquals(true, o);
	}
	
	@Test
	public void testPersonAgeLessOrGreater()
	{
		Person p = new Person();
		p.setAge(22);
		
		Object o = compileAndRun("age < 23", p);
		Assert.assertEquals(true, o);
		
		o = compileAndRun("age > 23", p);
		Assert.assertEquals(false, o);
		
		o = compileAndRun("age >= 22", p);
		Assert.assertEquals(true, o);
		
		o = compileAndRun("age <= 23", p);
		Assert.assertEquals(true, o);
	}
	
	@Test
	public void testAnd3()
	{
		Person p = new Person();
		p.setName("Name");
		p.setAge(87);
		
		Object o = compileAndRun("name == 'Name' && age == 87", p);
		
		Assert.assertEquals(true, o);
	}
	
	@Test
	public void testMapAccess()
	{
		TestMap map = new TestMap();
		map.put("entry", "value");
		
		Object o = compileAndRun("get('entry').class", map);
		Assert.assertEquals(String.class, o);
	}
	
	@Test
	public void testMapIndex()
	{
		IndexContainer container = new IndexContainer();
		
		Object o = compileAndRun("map['test']", container);
		Assert.assertEquals("one", o);
	}
	
	@Test
	public void testListIndex()
	{
		IndexContainer container = new IndexContainer();
		
		Object o = compileAndRun("list[0]", container);
		Assert.assertEquals("entry", o);
	}
	
	@Test
	public void testArrayIndex()
	{
		IndexContainer container = new IndexContainer();
		
		Object o = compileAndRun("array[0]", container);
		Assert.assertEquals("entry", o);
	}
	
	@Test
	public void testMultiStepIndex()
	{
		IndexContainer container = new IndexContainer();
		
		Object o = compileAndRun("multiStep['key'][0]", container);
		Assert.assertEquals("entry", o);
	}
	
	@Test
	public void testMultiStepArrayIndex()
	{
		IndexContainer container = new IndexContainer();
		
		Object o = compileAndRun("multiArray[0][0]", container);
		Assert.assertEquals("entry", o);
	}
	
	@Test
	public void testPersonVerified()
	{
		Person p = new Person();
		Object o = compileAndRun("verified", p);
		
		Assert.assertEquals(false, o);
	}
	
	@Test
	public void testAnd2()
	{
		Person p = new Person();
		p.setName("false");
		Object o = compileAndRun("verified && name", p);
		
		Assert.assertEquals(false, o);
	}
	
	@Test
	public void testLongToDoubleInMethod()
	{
		Container d = new Container();
		Object o = compileAndRun("ld.get(-10)", d);
		Assert.assertEquals(-10, o);
	}
	
	@Test
	public void testTernaryPrimitive()
	{
		LongToDouble d = new LongToDouble();
		Object o = compileAndRun("true ? 10 : 0", d);
		Assert.assertEquals(10l, o);
	}
	
	@Test
	public void testCalculateLong()
	{
		Assert.assertEquals(14l, compileAndRun("12 + 2", ""));
		Assert.assertEquals(10l, compileAndRun("12 - 2", ""));
		Assert.assertEquals(24l, compileAndRun("12 * 2", ""));
		Assert.assertEquals(6l, compileAndRun("12 / 2", ""));
		Assert.assertEquals(2l, compileAndRun("12 % 10", ""));
	}
	
	@Test
	public void testCalculateDouble()
	{
		Assert.assertEquals(14., compileAndRun("12.0 + 2", ""));
		Assert.assertEquals(10., compileAndRun("12.0 - 2", ""));
		Assert.assertEquals(24., compileAndRun("12.0 * 2", ""));
		Assert.assertEquals(6., compileAndRun("12.0 / 2", ""));
		Assert.assertEquals(2., compileAndRun("12.0 % 10", ""));
	}
	
	@Test
	public void testReturnThis()
	{
		Object t = new Object();
		Assert.assertSame(t, compileAndRun("this", t));
	}
	
	@Test
	public void testGetPublicField()
	{
		Person p = new Person();
		p.role = "test";
		
		Object o = compileAndRun("role", p);
		Assert.assertEquals("test", o);
	}
	
	@Test
	public void testLongArray()
	{
		Person p = new Person();
		
		Object o = compileAndRun("[ 1, 2 ]", p);
		if(! (o instanceof long[])) throw new AssertionError("Not a long array");
		assertArrayEquals(new long[] { 1l, 2l }, (long[]) o);
	}
	
	@Test
	public void testStringArray()
	{
		Person p = new Person();
		
		Object o = compileAndRun("[ 'value1', 'value2' ]", p);
		if(! (o instanceof String[])) throw new AssertionError("Not a string array");
		assertArrayEquals(new String[] { "value1", "value2" }, (String[]) o);
	}
	
	@Test
	public void testMixedArray()
	{
		Person p = new Person();
		p.setName("test");
		
		Object o = compileAndRun("[ 'value1', 2, name ]", p);
		if(! (o instanceof Object[])) throw new AssertionError("Not an object array");
		assertArrayEquals(new Object[] { "value1", 2l, "test" }, (Object[]) o);
	}
	
	@Test
	public void testGetReturnClass()
	{
		Expression expr = compile("this", Person.class);
		assertThat(expr.getReturnClass(), is((Object) Person.class));
		
		expr = compile("'static'", Person.class);
		assertThat(expr.getReturnClass(), is((Object) String.class));
	}
	
	
	private Object compileAndRun(String expr, Object in)
	{
		if(in == null) throw new NullPointerException("in must not be null");
		
		return compileAndRun(expr, in.getClass(), in);
	}
	
	private Expression compile(String expr, Class<?> context)
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
		
		ExpressionCompiler compiler = new ExpressionCompiler(errors, expr, context, invoker);
		return compiler.compile();
	}
	
	private Object compileAndRun(String expr, Class<?> context, Object in)
	{
		Expression compiled = compile(expr, context);
		return compiled.get(null, in);
	}
	
	public static class Container
	{
		private LongToDouble longToDouble;

		public Container()
		{
			longToDouble = new LongToDouble();
		}
		
		public LongToDouble getLd()
		{
			return longToDouble;
		}
	}
	
	public static class LongToDouble
	{
		public int get(double d)
		{
			return (int) d;
		}
	}
}
