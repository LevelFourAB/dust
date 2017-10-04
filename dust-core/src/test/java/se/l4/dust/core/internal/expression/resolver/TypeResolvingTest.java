package se.l4.dust.core.internal.expression.resolver;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.Test;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;

import se.l4.dust.core.internal.expression.invoke.TypeResolving;

/**
 * Tests for type resolving.
 *
 * @author Andreas Holstenson
 *
 */
public class TypeResolvingTest
{
	private TypeResolver resolver;

	public TypeResolvingTest()
	{
		resolver = new TypeResolver();
	}

	@Test
	public void testBAndC()
	{
		testCommon(
			new Class[] { B.class, C.class },
			new Class[] { A.class }
		);
	}

	@Test
	public void testBAndD()
	{
		testCommon(
			new Class[] { B.class, D.class },
			new Class[] { B.class, A.class }
		);
	}

	@Test
	public void testCAndD()
	{
		testCommon(
			new Class[] { C.class, D.class },
			new Class[] { A.class }
		);
	}

	@Test
	public void testI1AndI2()
	{
		testCommon(
			new Class[] { I1.class, I2.class },
			new Class[] { }
		);
	}

	@Test
	public void testInterfaceBubbling()
	{
		testCommon(
			new Class[] { E.class, E.class },
			new Class[] { E.class, I2.class }
		);
	}

	private static class A {}
	private static class B extends A {}
	private static class C extends A {}
	private static class D extends B implements I1 {}
	private static class E implements I2 {}

	private static interface I1 {}
	private static interface I2 {}

	private void testCommon(Class<?>[] types, Class<?>[] expected)
	{
		List<Class<?>> resolved = resolveCommonNonGeneric(types);

		List<Class<?>> actuallyExpected = new ArrayList<Class<?>>();
		for(Class<?> c : expected) actuallyExpected.add(c);

		Collections.sort(resolved, COMPARATOR);
		Collections.sort(actuallyExpected, COMPARATOR);

		assertThat(resolved, is(actuallyExpected));
	}

	private List<ResolvedType> resolveCommon(Class<?>... types)
	{
		List<ResolvedType> input = new ArrayList<ResolvedType>();
		for(Class<?> c : types)
		{
			input.add(resolver.resolve(c));
		}

		return TypeResolving.findCommonTypes(input);
	}

	private List<Class<?>> resolveCommonNonGeneric(Class<?>... types)
	{
		List<ResolvedType> items = resolveCommon(types);
		List<Class<?>> result = new ArrayList<Class<?>>();

		for(ResolvedType rt : items)
		{
			result.add(rt.getErasedType());
		}

		return result;
	}

	private final Comparator<Class> COMPARATOR = new Comparator<Class>()
	{
		@Override
		public int compare(Class o1, Class o2)
		{
			return o1.getSimpleName().compareTo(o2.getSimpleName());
		}
	};
}
