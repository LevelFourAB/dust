package se.l4.dust.core.internal.asset;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.inject.Guice;
import com.google.inject.Injector;

import junit.framework.Assert;
import se.l4.dust.api.Context;
import se.l4.dust.api.DefaultContext;
import se.l4.dust.api.Namespaces;
import se.l4.dust.api.asset.Asset;
import se.l4.dust.api.asset.AssetEncounter;
import se.l4.dust.api.asset.AssetProcessor;
import se.l4.dust.api.resource.MemoryResource;
import se.l4.dust.api.resource.Resources;
import se.l4.dust.api.resource.variant.ResourceVariant;
import se.l4.dust.api.resource.variant.ResourceVariantManager;
import se.l4.dust.core.CoreModule;
import se.l4.dust.core.internal.resource.ClasspathResourceLocator;
import se.l4.dust.core.internal.resource.LocaleVariantSource;

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
		instance.define("dust:test", "merged.txt")
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
		instance.define("dust:test", "merged.txt")
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
		instance.define("dust:test", "merged.txt")
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
	public void testNoVariantMergedWithPipeline()
		throws IOException
	{
		instance.define("dust:test", "merged-rev.txt")
			.add("file.txt", instance.pipeline(new ReverseStringProcessor()))
			.create();

		Context ctx = new DefaultContext();
		Asset asset = instance.locate(ctx, "dust:test", "merged-rev.txt");

		Assert.assertNotNull("no asset found", asset);
		Assert.assertEquals("merged-rev.txt", asset.getName());
		try(InputStream in = asset.getResource().openStream())
		{
			String actual = new String(ByteStreams.toByteArray(in), Charsets.UTF_8);
			assertThat(actual, is("elif"));
		}
	}


	@Test
	public void testVariantMergedSwedishLocalteWithPipeline()
		throws IOException
	{
		instance.define("dust:test", "merged-rev.txt")
			.add("file.txt", instance.pipeline(new ReverseStringProcessor()))
			.add("variant/file.txt")
			.create();

		Context ctx = new DefaultContext();
		ctx.putValue(ResourceVariant.LOCALE, new Locale("sv"));
		Asset asset = instance.locate(ctx, "dust:test", "merged-rev.txt");

		Assert.assertNotNull("no asset found", asset);
		Assert.assertEquals("merged-rev.sv.txt", asset.getName());
		try(InputStream in = asset.getResource().openStream())
		{
			String actual = new String(ByteStreams.toByteArray(in), Charsets.UTF_8);
			assertThat(actual, is("elifsv"));
		}
	}

	private class ReverseStringProcessor
		implements AssetProcessor
	{
		@Override
		public void process(AssetEncounter encounter)
			throws IOException
		{
			try(InputStream stream = encounter.getResource().openStream())
			{
				byte[] bytes = ByteStreams.toByteArray(stream);
				for(int i=0, t=bytes.length, n=bytes.length/2; i<n; i++)
				{
					byte b = bytes[i];
					bytes[i] = bytes[t - i - 1];
					bytes[t -  i - 1] = b;
				}

				encounter.replaceWith(new MemoryResource(encounter.getLocation(), "text/plain", "UTF-8", bytes));
			}
		}
	}
}
