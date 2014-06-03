package se.l4.dust.js.coffeescript;

import java.io.IOException;

import org.junit.Test;

import se.l4.crayon.Crayon;
import se.l4.crayon.CrayonModule;
import se.l4.dust.api.Namespace;
import se.l4.dust.api.NamespaceBinding;
import se.l4.dust.api.Namespaces;
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
		Injector injector = Guice.createInjector(Stage.DEVELOPMENT, new TestModule());
		injector.getInstance(Crayon.class).start();
		CoffeeScriptProcessor processor = injector.getInstance(CoffeeScriptProcessor.class);
		
		final UrlResource resource = new UrlResource(null, getClass().getResource("test.coffee"));
		processor.process(new AssetEncounter()
		{
			@Override
			public AssetEncounter replaceWith(Resource resource)
			{
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
			public Namespace getNamespace()
			{
				return null;
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
		public void bindNamespace(Namespaces manager)
		{
			manager.bind("dust:test")
				.setPackageFromClass(getClass())
				.add();
		}
	}
}
