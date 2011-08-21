package se.l4.dust.js.coffeescript;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import se.l4.dust.api.asset.AssetProcessor;
import se.l4.dust.api.resource.MemoryResource;
import se.l4.dust.api.resource.NamedResource;
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

	public Resource process(String namespace, String path, Resource in, Object... arguments)
		throws IOException
	{
		
		if(path.endsWith(".coffee"))
		{
			// Rewrite path to end with .css
			path = path.substring(0, path.length() - 5) + ".js";
		}
		
		InputStream stream = in.openStream();
		ByteArrayOutputStream out = new ByteArrayOutputStream(in.getContentLength());
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
		
		String value = new String(out.toByteArray(), in.getContentEncoding() != null ? in.getContentEncoding() : "UTF-8");
		
		try
		{
			Object result = new JavascriptEnvironment()
				.add(CoffeeScriptProcessor.class.getResource("coffee-script.js"))
				.add(CoffeeScriptProcessor.class.getResource("processor.js"))
				.define("code", value)
				.evaluate("compileResource(code);");
			
			MemoryResource res = new MemoryResource("text/javascript", "UTF-8", ((String) result).getBytes("UTF-8"));
			return new NamedResource(res, path);
		}
		catch(JavaScriptException e)
		{
			throw e;
//			throw processError(e);
		}
	}
	
	/**
	 * Attempt to get a bit better output from the processor when a syntax
	 * error occurs for the LESS file.
	 * 
	 * @param e
	 * @return
	 */
	private IOException processError(JavaScriptException e)
	{
		Scriptable value = (Scriptable) e.getValue();

		String name;
		if(ScriptableObject.hasProperty(value, "name"))
		{
			name = (String) ScriptableObject.getProperty(value, "name");
		}
		else if(ScriptableObject.hasProperty(value, "type"))
		{
			Object o = ScriptableObject.getProperty(value, "type");
			name = o instanceof String ? (String) o : "Error";
		}
		else
		{
			return new IOException(e.getMessage());
		}
		
		
		int line = ScriptableObject.hasProperty(value, "line")
			? ((Double) ScriptableObject.getProperty(value, "line")).intValue()
			: -1;
			
		int column = ScriptableObject.hasProperty(value, "column")
			? ((Double) ScriptableObject.getProperty(value, "column")).intValue()
			: -1;
			
		String message = ScriptableObject.hasProperty(value, "message")
			? (String) ScriptableObject.getProperty(value, "message")
			: "Error during LESS processing";
			
		return new IOException(name + ":" + message + " on line " + line + ", column " + column);
	}
}
