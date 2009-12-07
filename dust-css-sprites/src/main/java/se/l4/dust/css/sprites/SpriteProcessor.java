package se.l4.dust.css.sprites;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.io.FilenameUtils;
import org.carrot2.labs.smartsprites.SmartSpritesParameters;
import org.carrot2.labs.smartsprites.SpriteBuilder;
import org.carrot2.labs.smartsprites.SmartSpritesParameters.PngDepth;
import org.carrot2.labs.smartsprites.message.Message;
import org.carrot2.labs.smartsprites.message.MessageLog;
import org.carrot2.labs.smartsprites.message.MessageSink;
import org.carrot2.labs.smartsprites.message.Message.MessageLevel;
import org.carrot2.labs.smartsprites.resource.ResourceHandler;
import org.jdom.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import se.l4.dust.api.annotation.ContextScoped;
import se.l4.dust.api.asset.Asset;
import se.l4.dust.api.asset.AssetManager;
import se.l4.dust.api.asset.AssetProcessor;
import se.l4.dust.api.asset.MemoryResource;
import se.l4.dust.api.asset.Resource;

/**
 * Processor that uses SmartSprites to create automatic sprite images from
 * a CSS file.
 * 
 * @author Andreas Holstenson
 *
 */
@ContextScoped
public class SpriteProcessor
	implements AssetProcessor
{
	private static final Logger logger = LoggerFactory.getLogger(SpriteProcessor.class);
	
	private final AssetManager manager;
	private final ServletContext ctx;

	@Inject
	public SpriteProcessor(AssetManager manager, ServletContext ctx)
	{
		this.manager = manager;
		this.ctx = ctx;
	}
	
	public Resource process(Namespace namespace, String path, Resource stream)
		throws IOException
	{
		logger.info("Processing sprites in file " + path + " found in " + namespace.getURI());
		
		List<String> cssFiles = new LinkedList<String>();
		cssFiles.add("/" + path);
		
		SmartSpritesParameters params = new SmartSpritesParameters(
			"/",
			cssFiles,
			"/",
			"",
			MessageLevel.INFO,
			"",
			PngDepth.AUTO,
			true,
			"UTF-8"
		);
		
		MessageLog log = new MessageLog(
			new MessageSink()
			{
				public void add(Message message)
				{
					MessageLevel level = message.level;
					
					String msg = message.getFormattedMessage();
					switch(level)
					{
						case ERROR:
							logger.error(msg);
							break;
						case IE6NOTICE:
							logger.info("IE6NOTICE: " + msg);
							break;
						case INFO:
							logger.info(msg);
							break;
						case STATUS:
							logger.info("STATUS: " + msg);
							break;
						case WARN:
							logger.warn(msg);
							break;
					}
				}
			}
		);
		
		CustomResourceHandler handler = new CustomResourceHandler(manager, namespace, path, stream);
		SpriteBuilder builder = new SpriteBuilder(params, log, handler);
		builder.buildSprites();
		
		// Register any newly generated assets
		Resource resource = null;
		for(Map.Entry<String, ByteArrayOutputStream> e : handler.output.entrySet())
		{
			String key = e.getKey().substring(1);
			ByteArrayOutputStream out = e.getValue();
			
			String mimeType = ctx.getMimeType(key);
			Resource r = new MemoryResource(mimeType, null, out.toByteArray());
			
			if(key.equals(path))
			{
				// This is our main asset
				resource = r;
			}
			else
			{
				manager.addTemporaryAsset(namespace, key, r);
			}
		}
		
		return resource;
	}

	/**
	 * Resource handler used for conversion that integrates into the asset 
	 * management routines of Dust.
	 * 
	 * @author Andreas Holstenson
	 *
	 */
	private static class CustomResourceHandler
		implements ResourceHandler
	{
		private final AssetManager manager;
		private final Resource cssResource;
		private final String cssPath;
		private final Namespace ns;

		private final Map<String, ByteArrayOutputStream> output;
		
		public CustomResourceHandler(AssetManager manager, Namespace ns, String cssPath, Resource cssResource)
		{
			this.manager = manager;
			this.ns = ns;
			this.cssPath = "/" + cssPath;
			this.cssResource = cssResource;
			
			output = new HashMap<String, ByteArrayOutputStream>();
		}
		
		public InputStream getResourceAsInputStream(String path)
				throws IOException
		{
			Asset asset = manager.locate(ns, path);
			if(asset == null)
			{
				return null;
			}
			
			return asset.getResource().openStream();
		}

		public OutputStream getResourceAsOutputStream(final String path)
				throws IOException
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			output.put(path, out);
			
			return out;
		}

		public Reader getResourceAsReader(String path) throws IOException
		{
			if(cssPath.equals(path))
			{
				return new InputStreamReader(cssResource.openStream());
			}
			
			return null;
		}

		public Writer getResourceAsWriter(String path) throws IOException
		{
			return new OutputStreamWriter(getResourceAsOutputStream(path));
		}

		public String getResourcePath(String cssFilePath, String cssRelativePath)
		{
			if(cssRelativePath.startsWith("/"))
	        {
	            return cssRelativePath;
	        }
	        else if(cssFilePath.startsWith("//"))
	        {
	        	return FilenameUtils.concat(FilenameUtils.getFullPath(cssFilePath.substring(1)), cssRelativePath);
	        }
	        else
	        {
	            return FilenameUtils.concat(FilenameUtils.getFullPath(cssFilePath), cssRelativePath);
	        }
		}
		
	}
}
