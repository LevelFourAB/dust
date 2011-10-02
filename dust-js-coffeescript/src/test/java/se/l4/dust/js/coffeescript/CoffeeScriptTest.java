package se.l4.dust.js.coffeescript;

import java.io.IOException;

import org.junit.Test;

import se.l4.crayon.Crayon;
import se.l4.crayon.CrayonModule;
import se.l4.dust.api.NamespaceManager;
import se.l4.dust.api.annotation.NamespaceBinding;
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
		
		UrlResource resource = new UrlResource(getClass().getResource("test.coffee"));
		Resource result = processor.process("dust:test", "test.coffee", resource);
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
