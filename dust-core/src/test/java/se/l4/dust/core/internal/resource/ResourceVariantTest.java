package se.l4.dust.core.internal.resource;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.l4.dust.api.Context;
import se.l4.dust.api.DefaultContext;
import se.l4.dust.api.resource.variant.ResourceVariant;
import se.l4.dust.api.resource.variant.ResourceVariantManager;

/**
 * Test for the {@link ResourceVariantManager}.
 * 
 * @author Andreas Holstenson
 *
 */
public class ResourceVariantTest
{
	private ResourceVariantManagerImpl manager;

	@Before
	public void before()
	{
		manager = new ResourceVariantManagerImpl();
		manager.addSource(new LocaleVariantSource());
	}
	
	@Test
	public void testNoLocale()
		throws IOException
	{
		Context ctx = new DefaultContext();
		ResourceVariantManager.ResourceCallback callback = new ResourceVariantManager.ResourceCallback()
		{
			@Override
			public boolean exists(ResourceVariant variant, String url)
				throws IOException
			{
				Assert.fail("Tried checking if " + url + " exists");
				return false;
			}
		};
		
		manager.resolve(ctx, callback, "test.html");
	}
	
	@Test
	public void testWithLocale()
		throws IOException
	{
		Context ctx = new DefaultContext();
		ctx.putValue(ResourceVariant.LOCALE, Locale.ENGLISH);
		
		final AtomicBoolean checked = new AtomicBoolean();
		ResourceVariantManager.ResourceCallback callback = new ResourceVariantManager.ResourceCallback()
		{
			@Override
			public boolean exists(ResourceVariant variant, String url)
				throws IOException
			{
				checked.set(true);
				return false;
			}
		};
		
		manager.resolve(ctx, callback, "test.html");
		
		if(! checked.get()) Assert.fail("Did not look for english variant");
	}
	
	@Test
	public void testCacheWithLocale()
		throws IOException
	{
		Context ctx = new DefaultContext();
		ctx.putValue(ResourceVariant.LOCALE, Locale.ENGLISH);
		
		final AtomicBoolean checked = new AtomicBoolean();
		ResourceVariantManager.ResourceCallback callback = new ResourceVariantManager.ResourceCallback()
		{
			@Override
			public boolean exists(ResourceVariant variant, String url)
				throws IOException
			{
				checked.set(true);
				return false;
			}
		};
		
		manager.resolve(ctx, callback, "test.html");
		
		if(! checked.get()) Assert.fail("Did not look for english variant");
		
		// Reset for second run
		checked.set(false);
		
		manager.resolve(ctx, callback, "test.html");
		
		if(checked.get()) Assert.fail("Did not cache result of first call");
	}
}
