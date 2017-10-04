package se.l4.dust.yui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import se.l4.dust.api.asset.AssetEncounter;
import se.l4.dust.api.asset.AssetProcessor;
import se.l4.dust.api.resource.MemoryResource;
import se.l4.dust.api.resource.Resource;

import com.google.common.base.Charsets;
import com.google.common.io.Closeables;

/**
 * Processor that will compress CSS resources.
 *
 * @author Andreas Holstenson
 *
 */
public class CssCompressProcessor
	implements AssetProcessor
{

	public void process(AssetEncounter encounter)
		throws IOException
	{
		Resource resource = encounter.getResource();
		InputStream stream = resource.openStream();
		try
		{
			CssCompressor compressor = new CssCompressor(new InputStreamReader(stream, Charsets.UTF_8));

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			OutputStreamWriter writer = new OutputStreamWriter(out, Charsets.UTF_8);
			compressor.compress(writer, -1);
			writer.flush();

			MemoryResource res = new MemoryResource(
				resource.getContentType(),
				resource.getContentEncoding(),
				out.toByteArray()
			);

			encounter.replaceWith(res);
		}
		finally
		{
			Closeables.closeQuietly(stream);
		}
	}

}
