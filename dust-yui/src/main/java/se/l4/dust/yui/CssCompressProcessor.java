package se.l4.dust.yui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import se.l4.dust.api.asset.AssetProcessor;
import se.l4.dust.api.resource.MemoryResource;
import se.l4.dust.api.resource.Resource;

import com.google.common.base.Charsets;
import com.google.common.io.Closeables;
import com.yahoo.platform.yui.compressor.CssCompressor;

/**
 * Processor that will compress CSS resources.
 * 
 * @author Andreas Holstenson
 *
 */
public class CssCompressProcessor
	implements AssetProcessor
{

	public Resource process(String namespace, String path, Resource in, Object... arguments)
		throws IOException
	{
		InputStream stream = in.openStream();
		try
		{
			CssCompressor compressor = new CssCompressor(new InputStreamReader(stream, Charsets.UTF_8));
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			OutputStreamWriter writer = new OutputStreamWriter(out, Charsets.UTF_8);
			compressor.compress(writer, -1);
			writer.flush();
			
			return new MemoryResource(in.getContentType(), in.getContentEncoding(), out.toByteArray());
		}
		finally
		{
			Closeables.closeQuietly(stream);
		}
	}

}
