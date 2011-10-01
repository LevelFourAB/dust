package se.l4.dust.core.internal.asset;

import java.io.IOException;
import java.io.InputStream;

import se.l4.dust.api.asset.AssetProcessor;
import se.l4.dust.api.resource.MemoryResource;
import se.l4.dust.api.resource.Resource;

/**
 * Processor that will return the size of the input asset as text.
 * 
 * @author Andreas Holstenson
 *
 */
public class ByteCountProcessor
	implements AssetProcessor
{

	@Override
	public Resource process(String namespace, String path, Resource in,
			Object... arguments)
		throws IOException
	{
		int length = in.getContentLength();
		if(length < 0)
		{
			length = 0;
			InputStream stream = in.openStream();
			while(stream.read() != -1) length++;
			stream.close();
		}
		
		byte[] data = String.valueOf(length).getBytes();
		return new MemoryResource(null, null, data);
	}

}
