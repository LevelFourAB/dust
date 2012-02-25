package se.l4.dust.js.coffeescript;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import se.l4.crayon.Crayon;
import se.l4.crayon.CrayonModule;
import se.l4.dust.api.NamespaceManager;
import se.l4.dust.api.NamespaceManager.Namespace;
import se.l4.dust.api.annotation.NamespaceBinding;
import se.l4.dust.api.asset.AssetEncounter;
import se.l4.dust.api.resource.Resource;
import se.l4.dust.api.resource.UrlResource;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;

public class CoffeeScriptTest
{
	@Test
	public void testCompilation()
		throws IOException
	{
		Injector injector = Guice.createInjector(Stage.DEVELOPMENT, new CoffeeScriptModule(), new TestModule());
		injector.getInstance(Crayon.class).start();
		CoffeeScriptProcessor processor = injector.getInstance(CoffeeScriptProcessor.class);
		
		final UrlResource resource = new UrlResource(getClass().getResource("test.coffee"));
		processor.process(new AssetEncounter()
		{
			@Override
			public AssetEncounter replaceWith(Resource resource)
			{
				return this;
			}
			
			@Override
			public AssetEncounter rename(String name)
			{
				assertEquals("test.js", name);
				return this;
			}
			
			@Override
			public boolean isProduction()
			{
				return false;
			}
			
			@Override
			public Resource getResource()
			{
				return resource;
			}
			
			@Override
			public String getPath()
			{
				return "test.coffee";
			}
			
			@Override
			public Namespace getNamespaceObject()
			{
				return null;
			}
			
			@Override
			public String getNamepace()
			{
				return "dust:test";
			}
			
			@Override
			public AssetEncounter cache(String id, Resource resource)
			{
				return this;
			}
			
			@Override
			public Resource getCached(String id)
			{
				return null;
			}
		});
	}
	
	public static class TestModule
		extends CrayonModule
	{
		@Override
		protected void configure()
		{
		}
		
		@NamespaceBinding
		public void bindNamespace(NamespaceManager manager)
		{
			manager.bind("dust:test")
				.setPackageFromClass(getClass())
				.add();
		}
	}
}
