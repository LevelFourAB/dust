package se.l4.dust.core.internal.expression;

import org.junit.Assert;
import org.junit.Test;

import se.l4.dust.api.expression.ExpressionSource;
import se.l4.dust.api.template.dom.Element;

/**
 * Test for {@link CommonSource}.
 * 
 * @author Andreas Holstenson
 *
 */
public class CommonSourceTest
	extends AbstractSourceTest
{

	@Override
	protected ExpressionSource createSource()
	{
		return new CommonSource();
	}

	@Test
	public void testEmit()
	{
		Assert.assertEquals(execute("t:emit", ""), Element.Attribute.ATTR_EMIT);
		Assert.assertEquals(execute("true ? t:skip", ""), Element.Attribute.ATTR_SKIP);
	}
	
	@Test
	public void testSkip()
	{
		Assert.assertEquals(execute("t:skip", ""), Element.Attribute.ATTR_SKIP);
		Assert.assertEquals(execute("true ? t:skip", ""), Element.Attribute.ATTR_SKIP);
	}
	
	@Test
	public void testEncode()
	{
		Assert.assertEquals(execute("t:urlencode('value')", ""), "value");
	}
}
