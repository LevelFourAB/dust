package se.l4.dust.core.internal.resource;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import se.l4.dust.api.Context;
import se.l4.dust.api.DefaultContext;
import se.l4.dust.api.Namespace;
import se.l4.dust.api.resource.MemoryResource;
import se.l4.dust.api.resource.NamespaceLocation;
import se.l4.dust.api.resource.Resource;
import se.l4.dust.api.resource.variant.ResourceVariant;
import se.l4.dust.api.resource.variant.ResourceVariantManager;
import se.l4.dust.api.resource.variant.ResourceVariantResolution;

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
		ResourcesImpl resources = new ResourcesImpl();
		
		manager = new ResourceVariantManagerImpl(resources);
		manager.addSource(new LocaleVariantSource());
	}
	
	@Test
	public void testNoLocale()
		throws IOException
	{
		Context ctx = new DefaultContext();
		ResourceVariantResolution result = manager.resolve(ctx, new NamespaceLocation(createFakeNamespace(), "test.html"));
		assertThat(result.getName(), is("test.html"));
	}
	
	@Test
	public void testWithLocale()
		throws IOException
	{
		Context ctx = new DefaultContext();
		ctx.putValue(ResourceVariant.LOCALE, Locale.ENGLISH);
		ResourceVariantResolution result = manager.resolve(ctx, new NamespaceLocation(createFakeNamespace(), "test.html"));
		assertThat(result.getName(), is("test.en.html"));
	}
	
	private Namespace createFakeNamespace()
	{
		return new Namespace()
		{
			@Override
			public String getVersion()
			{
				return null;
			}
			
			@Override
			public String getUri()
			{
				return null;
			}
			
			@Override
			public Resource getResource(String resource)
				throws IOException
			{
				return new MemoryResource("", "", (byte[]) null);
			}
			
			@Override
			public String getPrefix()
			{
				return null;
			}
			
			@Override
			public String getPackage()
			{
				return null;
			}
			
			@Override
			public URL getClasspathResource(String resource)
			{
				return null;
			}
		};
	}
}
