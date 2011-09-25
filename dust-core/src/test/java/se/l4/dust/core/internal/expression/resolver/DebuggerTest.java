package se.l4.dust.core.internal.expression.resolver;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import se.l4.crayon.Crayon;
import se.l4.dust.api.conversion.TypeConverter;
import se.l4.dust.core.internal.conversion.ConversionModule;
import se.l4.dust.core.internal.expression.ExpressionDebugger;
import se.l4.dust.core.internal.expression.ExpressionsImpl;
import se.l4.dust.core.internal.expression.model.Person;

import com.google.inject.Guice;
import com.google.inject.Injector;

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
		expressions = new ExpressionsImpl(tc);
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
