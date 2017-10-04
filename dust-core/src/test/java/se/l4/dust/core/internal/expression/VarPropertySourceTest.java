package se.l4.dust.core.internal.expression;

import junit.framework.Assert;

import org.junit.Test;

import se.l4.dust.api.Context;
import se.l4.dust.api.DefaultContext;
import se.l4.dust.api.expression.Expression;
import se.l4.dust.api.expression.ExpressionSource;

public class VarPropertySourceTest
	extends AbstractSourceTest
{
	@Test
	public void testSimpleGet()
	{
		Expression expr = compile("t:x", String.class);
		Context ctx = new DefaultContext();
		Object o = expr.get(ctx, "test");

		Assert.assertEquals(null, o);
	}

	@Test
	public void testSimpleSet()
	{
		Expression expr = compile("t:x", String.class);

		Context ctx = new DefaultContext();
		expr.set(ctx, "test", "value");

		Object o = expr.get(ctx, "test");
		Assert.assertEquals("value", o);
	}

	@Test
	public void testInMethod()
	{
		Expression expr = compile("t:x", String.class);

		Context ctx = new DefaultContext();
		expr.set(ctx, "test", 2);

		Expression e2 = compile("charAt(t:x)", String.class);
		Object o = e2.get(ctx, "test");
		Assert.assertEquals('s', o);
	}

	@Override
	protected ExpressionSource createSource()
	{
		return new VarPropertySource();
	}

}
