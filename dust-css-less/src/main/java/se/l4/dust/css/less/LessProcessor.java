package se.l4.dust.css.less;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import se.l4.dust.api.DefaultContext;
import se.l4.dust.api.asset.Asset;
import se.l4.dust.api.asset.AssetException;
import se.l4.dust.api.asset.AssetManager;
import se.l4.dust.api.asset.AssetProcessor;
import se.l4.dust.api.resource.MemoryResource;
import se.l4.dust.api.resource.NamedResource;
import se.l4.dust.api.resource.Resource;
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
	private final AssetManager assets;

	@Inject
	public LessProcessor(Stage stage, AssetManager assets)
	{
		this.assets = assets;
		
		development = stage != Stage.PRODUCTION;
	}

	public Resource process(String namespace, String path, Resource in, Object... arguments)
		throws IOException
	{
		if(path.endsWith(".less"))
		{
			// Rewrite path to end with .css
			path = path.substring(0, path.length() - 5) + ".css";
		}
		
		int idx = path.lastIndexOf('/');
		String folder = idx > 0 ? path.substring(0, idx+1) : "";
		
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
			Object result = new JavascriptEnvironment(getClass().getName())
				.define("development", development)
				.define("importer", new Importer(assets, namespace, folder))
				.add(LessProcessor.class.getResource("env.js"))
				.add(LessProcessor.class.getResource("less-1.1.4.js"))
				.add(LessProcessor.class.getResource("processor.js"))
				.define("css", value)
				.evaluate("compileResource(css);");
			
			MemoryResource res = new MemoryResource("text/css", "UTF-8", ((String) result).getBytes("UTF-8"));
			return new NamedResource(res, path);
		}
		catch(JavaScriptException e)
		{
			throw processError(e);
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
		
		
		int line = hasProperty(value, "line")
			? ((Double) ScriptableObject.getProperty(value, "line")).intValue()
			: -1;
			
		int column = hasProperty(value, "column")
			? ((Double) ScriptableObject.getProperty(value, "column")).intValue()
			: -1;
			
		String message = hasProperty(value, "message")
			? (String) ScriptableObject.getProperty(value, "message")
			: "Error during LESS processing";
			
		return new IOException(name + ": " + message + " on line " + line + ", column " + column);
	}
	
	private boolean hasProperty(Scriptable obj, String prop)
	{
		return ScriptableObject.hasProperty(obj, prop)
			&& ScriptableObject.getProperty(obj, prop) != null;
	}
	
	public static class Importer
	{
		private final AssetManager assets;
		private final String namespace;
		private final String folder;

		public Importer(AssetManager assets, String namespace, String folder)
		{
			this.namespace = namespace;
			this.assets = assets;
			this.folder = folder;
		}
		
		public Object read(String name)
			throws IOException
		{
			Asset asset = assets.locate(new DefaultContext(), namespace, folder + "" + name);
			if(asset == null)
			{
				throw new AssetException("Unable to process, " + name + " was not found in " + namespace);
			}
			
			InputStream stream = asset.getResource().openStream();
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
