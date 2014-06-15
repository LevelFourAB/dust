package se.l4.dust.css.less;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import se.l4.dust.api.asset.AssetEncounter;
import se.l4.dust.api.asset.AssetException;
import se.l4.dust.api.asset.AssetProcessor;
import se.l4.dust.api.resource.MemoryResource;
import se.l4.dust.api.resource.Resource;
import se.l4.dust.api.resource.ResourceLocation;
import se.l4.dust.api.resource.Resources;
import se.l4.dust.js.env.JavascriptEnvironment;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.google.inject.Inject;
import com.google.inject.Stage;

/**
 * Processor of LESS style sheets. This processor will convert LESS files into
 * pure CSS files on the fly.
 * 
 * @author Andreas Holstenson
 *
 */
public class LessProcessor
	implements AssetProcessor
{
	private final boolean development;
	private final Resources resources;

	@Inject
	public LessProcessor(Stage stage, Resources resources)
	{
		this.resources = resources;
		
		development = stage != Stage.PRODUCTION;
	}

	@Override
	public void process(AssetEncounter encounter)
		throws IOException
	{
		Resource cached = encounter.getCached("lesscss");
		if(cached != null)
		{
			encounter.replaceWith(cached);
			return;
		}
		
		String path = encounter.getLocation().getName();
		if(path.endsWith(".less"))
		{
			// Rewrite path to end with .css
			path = path.substring(0, path.length() - 5) + ".css";
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
			Object result = new JavascriptEnvironment(getClass().getName())
				.define("development", development)
				.define("importer", new Importer(resources, encounter.getLocation()))
				.add(LessProcessor.class.getResource("env.js"))
				.add(LessProcessor.class.getResource("less-1.5.0.js"))
				.add(LessProcessor.class.getResource("processor.js"))
				.define("css", value)
				.evaluate("compileResource(css);");
			
			MemoryResource res = new MemoryResource("text/css", "UTF-8", ((String) result).getBytes("UTF-8"));
			if(! development)
			{
				encounter.cache("lesscss", res);
			}
			
			encounter.replaceWith(res);
		}
		catch(JavaScriptException e)
		{
			throw processError(value, e);
		}
	}
	
	/**
	 * Attempt to get a bit better output from the processor when a syntax
	 * error occurs for the LESS file.
	 * @param value2 
	 * 
	 * @param e
	 * @return
	 */
	private IOException processError(String lessValue, JavaScriptException e)
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
		
		
		int line = hasProperty(value, "line")
			? ((Double) ScriptableObject.getProperty(value, "line")).intValue()
			: -1;
			
		int column = hasProperty(value, "column")
			? ((Double) ScriptableObject.getProperty(value, "column")).intValue()
			: -1;
			
		CharSequence message = hasProperty(value, "message")
			? (CharSequence) ScriptableObject.getProperty(value, "message")
			: "Error during LESS processing";
		
		// Extract the line
		int current = 0;
		String text = null;
		try
		{
			BufferedReader reader = new BufferedReader(new StringReader(lessValue));
			while((text = reader.readLine()) != null)
			{
				if(++current == line)
				{
					break;
				}
			}
		}
		catch(IOException e0)
		{
		}
		
		return new IOException(name + ": " + message + " (line " + line + ", column " + column + ")\n\n\t" + text);
	}
	
	private boolean hasProperty(Scriptable obj, String prop)
	{
		return ScriptableObject.hasProperty(obj, prop)
			&& ScriptableObject.getProperty(obj, prop) != null;
	}
	
	public static class Importer
	{
		private final Resources resources;
		private final ResourceLocation location;

		public Importer(Resources resources, ResourceLocation location)
		{
			this.location = location;
			this.resources = resources;
		}
		
		public Object read(String name)
			throws IOException
		{
			ResourceLocation newLocation = location.resolve(name);
			Resource resource = resources.locate(newLocation);
			if(resource == null)
			{
				throw new AssetException("Unable to process, " + newLocation + " was not found");
			}
			
			InputStream stream = resource.openStream();
			try
			{
				byte[] data = ByteStreams.toByteArray(stream);
				return Context.javaToJS(new String(data, Charsets.UTF_8), null);
			}
			finally
			{
				Closeables.closeQuietly(stream);
			}
		}
	}
}
