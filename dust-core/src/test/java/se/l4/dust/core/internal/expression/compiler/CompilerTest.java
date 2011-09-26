package se.l4.dust.core.internal.expression.compiler;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

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
		expressions = new ExpressionsImpl(tc);
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
	
	private Object compileAndRun(String expr, Object in)
	{
		if(in == null) throw new NullPointerException("in must not be null");
		
		return compileAndRun(expr, in.getClass(), in);
	}
	
	private Object compileAndRun(String expr, Class<?> context, Object in)
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
		
		ExpressionCompiler compiler = new ExpressionCompiler(errors, context, invoker);
		Expression compiled = compiler.compile();
		return compiled.get(null, in);
	}
}
