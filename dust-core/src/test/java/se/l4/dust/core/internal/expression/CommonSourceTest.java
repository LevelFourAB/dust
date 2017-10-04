package se.l4.dust.core.internal.expression;

import org.junit.Assert;
import org.junit.Test;

import com.google.inject.Stage;

import se.l4.dust.api.expression.ExpressionSource;
import se.l4.dust.api.template.dom.Attribute;
import se.l4.dust.core.internal.expression.model.Person;

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
		return new CommonSource(Stage.PRODUCTION);
	}

	@Test
	public void testEmit()
	{
		Assert.assertEquals(Attribute.ATTR_EMIT, execute("t:emit", ""));
		Assert.assertEquals(Attribute.ATTR_EMIT, execute("true ? t:emit", ""));
	}

	@Test
	public void testSkip()
	{
		Assert.assertEquals(Attribute.ATTR_SKIP, execute("t:skip", ""));
		Assert.assertEquals(Attribute.ATTR_SKIP, execute("true ? t:skip", ""));
	}

	@Test
	public void testEncode()
	{
		Assert.assertEquals("value", execute("t:urlencode('value')", ""));
	}

	@Test
	public void testEncodeChain()
	{
		Assert.assertEquals(5, execute("t:urlencode('value').length()", ""));
	}

	@Test
	public void testEncodeOnObject()
	{
		Person p = new Person();
		p.setName("Name");
		Assert.assertEquals("Name", execute("name.t:urlencode()", p));
	}
}
