package se.l4.dust.js.coffeescript;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.mozilla.javascript.JavaScriptException;

import se.l4.dust.api.asset.AssetEncounter;
import se.l4.dust.api.asset.AssetProcessor;
import se.l4.dust.api.resource.MemoryResource;
import se.l4.dust.api.resource.Resource;
import se.l4.dust.js.env.JavascriptEnvironment;

/**
 * Processor of CoffeeScripts.
 * 
 * @author Andreas Holstenson
 *
 */
public class CoffeeScriptProcessor
	implements AssetProcessor
{
	private static final String EXTENSION = ".coffee";

	@Override
	public void process(AssetEncounter encounter)
		throws IOException
	{
		Resource cached = encounter.getCached("coffeescript");
		if(cached != null)
		{
			encounter.replaceWith(cached);
			return;
		}
		
		Resource resource = encounter.getResource();
		InputStream stream = resource.openStream();
		ByteArrayOutputStream out = new ByteArrayOutputStream(resource.getContentLength());
		try
		{
			int len = 0;
			byte[] buf = new byte[1024];
			while((len = stream.read(buf)) != -1)
			{
				out.write(buf, 0, len);
			}
		}
		finally
		{
			stream.close();
		}
		
		String value = new String(out.toByteArray(), resource.getContentEncoding() != null ? resource.getContentEncoding() : "UTF-8");
		
		try
		{
			Object result = new JavascriptEnvironment()
				.add(CoffeeScriptProcessor.class.getResource("processor.js"))
				.add(CoffeeScriptProcessor.class.getResource("coffee-script.js"))
				.define("code", value)
				.evaluate("compileResource(code);");
			
			MemoryResource res = new MemoryResource("text/javascript", "UTF-8", ((String) result).getBytes("UTF-8"));
			encounter
				.cache("coffeescript", res)
				.replaceWith(res);
		}
		catch(JavaScriptException e)
		{
			throw e;
//			throw processError(e);
		}
	}
}
