package se.l4.dust.core.internal.expression.resolver;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;

import se.l4.crayon.Crayon;
import se.l4.dust.api.conversion.TypeConverter;
import se.l4.dust.api.expression.ExpressionException;
import se.l4.dust.core.internal.conversion.ConversionModule;
import se.l4.dust.core.internal.expression.ExpressionDebugger;
import se.l4.dust.core.internal.expression.ExpressionsImpl;
import se.l4.dust.core.internal.expression.model.Person;

/**
 * Tests that run expression in debug mode (interpreted) and verify that
 * they return the intended results.
 * 
 * @author Andreas Holstenson
 *
 */
public class DebuggerTest
{
	private TypeConverter tc;
	private ExpressionsImpl expressions;

	@Before
	public void before()
	{
		Injector injector = Guice.createInjector(new ConversionModule());
		injector.getInstance(Crayon.class).start();
		tc = injector.getInstance(TypeConverter.class);
		expressions = new ExpressionsImpl(tc, Stage.DEVELOPMENT);
	}
	
	@Test
	public void testGetClass()
	{
		Object o = debug("class", "string");
		
		Assert.assertEquals(String.class, o);
	}
	
	@Test
	public void testNullPersonName()
	{
		Person p = new Person();
		Object o = debug("name", p);
		
		Assert.assertEquals(null, o);
	}
	
	@Test
	public void testPersonName()
	{
		Person p = new Person();
		p.setName("Name");
		Object o = debug("name", p);
		
		Assert.assertEquals("Name", o);
	}
	
	@Test
	public void testPersonNameLength()
	{
		Person p = new Person();
		p.setName("Name");
		Object o = debug("name.length()", p);
		
		Assert.assertEquals(4, o);
	}
	
	@Test
	public void testPersonNameIs()
	{
		Person p = new Person();
		p.setName("One");
		
		Object o = debug("name == 'One'", p);
		
		Assert.assertEquals(true, o);
		
		o = debug("name != 'One'", p);
		Assert.assertEquals(false, o);
	}
	
	@Test
	public void testPersonAge()
	{
		Person p = new Person();
		p.setAge(22);
		
		Object o = debug("age == 22", p);
		Assert.assertEquals(true, o);
		
		o = debug("age != 23", p);
		Assert.assertEquals(true, o);
	}
	
	@Test
	public void testPersonAgeLessOrGreater()
	{
		Person p = new Person();
		p.setAge(22);
		
		Object o = debug("age < 23", p);
		Assert.assertEquals(true, o);
		
		o = debug("age > 23", p);
		Assert.assertEquals(false, o);
		
		o = debug("age >= 22", p);
		Assert.assertEquals(true, o);
		
		o = debug("age <= 23", p);
		Assert.assertEquals(true, o);
	}
	
	@Test
	public void testAnd()
	{
		Person p = new Person();
		p.setName("Name");
		p.setAge(87);
		
		Object o = debug("name == 'Name' && age == 87", p);
		
		Assert.assertEquals(true, o);
	}
	
	@Test
	public void testMapAccess()
	{
		TestMap map = new TestMap();
		map.put("entry", "value");
		
		Object o = debug("get('entry').class", map);
		Assert.assertEquals(String.class, o);
	}
	
	@Test
	public void testMapIndex()
	{
		IndexContainer container = new IndexContainer();
		
		Object o = debug("map['test']", container);
		Assert.assertEquals("one", o);
	}
	
	@Test
	public void testListIndex()
	{
		IndexContainer container = new IndexContainer();
		
		Object o = debug("list[0]", container);
		Assert.assertEquals("entry", o);
	}
	
	@Test
	public void testArrayIndex()
	{
		IndexContainer container = new IndexContainer();
		
		Object o = debug("array[0]", container);
		Assert.assertEquals("entry", o);
	}
	
	@Test
	public void testMultiStepIndex()
	{
		IndexContainer container = new IndexContainer();
		
		Object o = debug("multiStep['key'][0]", container);
		Assert.assertEquals("entry", o);
	}
	
	@Test
	public void testMultiStepArrayIndex()
	{
		IndexContainer container = new IndexContainer();
		
		Object o = debug("multiArray[0][0]", container);
		Assert.assertEquals("entry", o);
	}
	
	@Test
	public void testPersonVerified()
	{
		Person p = new Person();
		Object o = debug("verified", p);
		
		Assert.assertEquals(false, o);
	}
	
	@Test
	public void testAnd2()
	{
		Person p = new Person();
		p.setName("false");
		Object o = debug("verified && name", p);
		
		Assert.assertEquals(false, o);
	}
	
	@Test
	public void testPersonNullFailure()
	{
		try
		{
			debug("verified", Person.class, null);
		
			Assert.fail();
		}
		catch(ExpressionException e)
		{
		}
	}
	
	@Test
	public void testPersonInnerFailure()
	{
		try
		{
			Person p = new Person();
			debug("name.length()", p);
		
			Assert.fail();
		}
		catch(ExpressionException e)
		{
		}
	}
	
	@Test
	public void testCalculateLong()
	{
		Assert.assertEquals(14l, debug("12 + 2", ""));
		Assert.assertEquals(10l, debug("12 - 2", ""));
		Assert.assertEquals(24l, debug("12 * 2", ""));
		Assert.assertEquals(6l, debug("12 / 2", ""));
		Assert.assertEquals(2l, debug("12 % 10", ""));
	}
	
	@Test
	public void testCalculateDouble()
	{
		Assert.assertEquals(14., debug("12.0 + 2", ""));
		Assert.assertEquals(10., debug("12.0 - 2", ""));
		Assert.assertEquals(24., debug("12.0 * 2", ""));
		Assert.assertEquals(6., debug("12.0 / 2", ""));
		Assert.assertEquals(2., debug("12.0 % 10", ""));
	}
	
	@Test
	public void testReturnThis()
	{
		Object t = new Object();
		Assert.assertSame(t, debug("this", t));
	}
	
	@Test
	public void testGetPublicField()
	{
		Person p = new Person();
		p.role = "test";
		
		Object o = debug("role", p);
		Assert.assertEquals("test", o);
	}
	
	@Test
	public void testAndChain()
	{
		Person p = new Person();
		
		Object o = debug("name != null && name.length() > 0", p);
		Assert.assertEquals(false, o);
	}
	
	@Test
	public void testLongArray()
	{
		Person p = new Person();
		
		Object o = debug("[ 1, 2 ]", p);
		if(! (o instanceof long[])) throw new AssertionError("Not a long array");
		assertArrayEquals(new long[] { 1l, 2l }, (long[]) o);
	}
	
	@Test
	public void testStringArray()
	{
		Person p = new Person();
		
		Object o = debug("[ 'value1', 'value2' ]", p);
		if(! (o instanceof String[])) throw new AssertionError("Not a string array");
		assertArrayEquals(new String[] { "value1", "value2" }, (String[]) o);
	}
	
	@Test
	public void testMixedArray()
	{
		Person p = new Person();
		p.setName("test");
		
		Object o = debug("[ 'value1', 2, name ]", p);
		if(! (o instanceof Object[])) throw new AssertionError("Not an object array");
		assertArrayEquals(new Object[] { "value1", 2l, "test" }, (Object[]) o);
	}
	
	public static class IndexContainer
	{
		public Map<String, String> getMap()
		{
			Map<String, String> result = new HashMap<String, String>();
			result.put("test", "one");
			return result;
		}
		
		public List<String> getList()
		{
			return Arrays.asList("entry");
		}
		
		public String[] getArray()
		{
			return new String[] { "entry" };
		}
		
		public String[][] getMultiArray()
		{
			return new String[][] {
				{ "entry" }
			};
		}
		
		public Map<String, List<String>> getMultiStep()
		{
			Map<String, List<String>> result = new HashMap<String, List<String>>();
			result.put("key", Arrays.asList("entry"));
			return result;
		}
	}
	
	private Object debug(String expr, Object in)
	{
		if(in == null) throw new NullPointerException("in must not be null");
		
		return debug(expr, in.getClass(), in);
	}
	
	private Object debug(String expr, Class<?> context, Object in)
	{
		ExpressionDebugger debugger = new ExpressionDebugger(
			tc, 
			expressions,
			Collections.<String, String>emptyMap(),
			expr, 
			context
		);
		
		return debugger.get(null, in);
	}
	
	public static class TestMap
		extends HashMap<String, String>
	{
	}
}
