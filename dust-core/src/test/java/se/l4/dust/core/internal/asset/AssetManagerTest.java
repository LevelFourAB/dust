package se.l4.dust.core.internal.asset;

import java.io.InputStream;
import java.util.Locale;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import se.l4.dust.api.Context;
import se.l4.dust.api.DefaultContext;
import se.l4.dust.api.Namespaces;
import se.l4.dust.api.asset.Asset;
import se.l4.dust.api.resource.Resource;
import se.l4.dust.api.resource.Resources;
import se.l4.dust.api.resource.variant.ResourceVariant;
import se.l4.dust.api.resource.variant.ResourceVariantManager;
import se.l4.dust.core.CoreModule;
import se.l4.dust.core.internal.resource.ClasspathResourceLocator;
import se.l4.dust.core.internal.resource.LocaleVariantSource;

import com.google.common.io.ByteStreams;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Tests for asset management.
 * 
 * @author Andreas Holstenson
 *
 */
public class AssetManagerTest
{
	private AssetsImpl instance;
	
	/**
	 * Setup the tests. Creates an {@link Injector} containing the core
	 * module. It registers the package of the class as a namespace allowing
	 * us to access test resources. Finally adds the {@link LocaleVariantSource}
	 * so that we can test variants.
	 */
	@Before
	public void before()
	{
		Injector injector = Guice.createInjector(new CoreModule());
		
		injector.getInstance(Namespaces.class)
			.bind("dust:test")
			.setPackageFromClass(getClass())
			.add();
		
		instance = injector.getInstance(AssetsImpl.class);
		injector.getInstance(Resources.class).addLocator(injector.getInstance(ClasspathResourceLocator.class));
		
		injector.getInstance(ResourceVariantManager.class)
			.addSource(new LocaleVariantSource());
	}
	
	@Test
	public void testNoVariant()
	{
		Context ctx = new DefaultContext();
		Asset asset = instance.locate(ctx, "dust:test", "file.txt");
		
		Assert.assertNotNull("no asset found", asset);
		Assert.assertEquals("file.txt", asset.getName());
	}
	
	@Test
	public void testVariantNoLocale()
	{
		Context ctx = new DefaultContext();
		Asset asset = instance.locate(ctx, "dust:test", "variant/file.txt");
		
		Assert.assertNotNull("no asset found", asset);
		Assert.assertEquals("variant/file.txt", asset.getName());
	}
	
	@Test
	public void testVariantSwedishLocale()
	{
		Context ctx = new DefaultContext();
		ctx.putValue(ResourceVariant.LOCALE, new Locale("sv"));
		Asset asset = instance.locate(ctx, "dust:test", "variant/file.txt");
		
		Assert.assertNotNull("no asset found", asset);
		Assert.assertEquals("variant/file.sv.txt", asset.getName());
	}
	
	@Test
	public void testNoVariantMerged()
	{
		instance.addAsset("dust:test", "merged.txt")
			.add("file.txt")
			.create();
		
		Context ctx = new DefaultContext();
		Asset asset = instance.locate(ctx, "dust:test", "merged.txt");
		
		Assert.assertNotNull("no asset found", asset);
		Assert.assertEquals("merged.txt", asset.getName());
	}
	
	@Test
	public void testVariantMergedNoLocale()
	{
		instance.addAsset("dust:test", "merged.txt")
			.add("file.txt")
			.add("variant/file.txt")
			.create();
		
		Context ctx = new DefaultContext();
		Asset asset = instance.locate(ctx, "dust:test", "merged.txt");
		
		Assert.assertNotNull("no asset found", asset);
		Assert.assertEquals("merged.txt", asset.getName());
	}
	
	@Test
	public void testVariantMergedSwedishLocale()
	{
		instance.addAsset("dust:test", "merged.txt")
			.add("file.txt")
			.add("variant/file.txt")
			.create();
		
		Context ctx = new DefaultContext();
		ctx.putValue(ResourceVariant.LOCALE, new Locale("sv"));
		Asset asset = instance.locate(ctx, "dust:test", "merged.txt");
		
		Assert.assertNotNull("no asset found", asset);
		Assert.assertEquals("merged.sv.txt", asset.getName());
	}
	
	@Test
	public void testNoVariantProcessed()
	{
		instance.processAssets("dust:test", "file.txt", ByteCountProcessor.class);
		
		Context ctx = new DefaultContext();
		Asset asset = instance.locate(ctx, "dust:test", "file.txt");
		
		Assert.assertNotNull("no asset found", asset);
		Assert.assertEquals("file.txt", asset.getName());
		
		Assert.assertEquals(4, getCount(asset.getResource()));
	}
	
	@Test
	public void testVariantProcessedNoLocale()
	{
		instance.processAssets("dust:test", "variant/file.txt", ByteCountProcessor.class);
		
		Context ctx = new DefaultContext();
		Asset asset = instance.locate(ctx, "dust:test", "variant/file.txt");
		
		Assert.assertNotNull("no asset found", asset);
		Assert.assertEquals("variant/file.txt", asset.getName());
		
		Assert.assertEquals(2, getCount(asset.getResource()));
	}
	
//	@Test
	// Test disabled until asset management can be rewritten
	public void testVariantProcessedSwedishLocale()
	{
		instance.processAssets("dust:test", "variant/file.txt", ByteCountProcessor.class);
		
		Context ctx = new DefaultContext();
		ctx.putValue(ResourceVariant.LOCALE, new Locale("sv"));
		Asset asset = instance.locate(ctx, "dust:test", "variant/file.txt");
		
		Assert.assertNotNull("no asset found", asset);
		Assert.assertEquals("variant/file.sv.txt", asset.getName());
		
		Assert.assertEquals(2, getCount(asset.getResource()));
	}
	
	private int getCount(Resource in)
	{
		try
		{
			InputStream stream = in.openStream();
			byte[] data = ByteStreams.toByteArray(stream);
			stream.close();
			
			return Integer.parseInt(new String(data));
		}
		catch(Exception e)
		{
			Assert.fail("Unable to get count");
			return 0;
		}
	}
}
