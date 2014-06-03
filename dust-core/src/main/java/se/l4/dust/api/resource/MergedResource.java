package se.l4.dust.api.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;


/**
 * Resource that consists of several other resources.
 * 
 * @author Andreas Holstenson
 *
 */
public class MergedResource
	extends AbstractResource
{
	private final Resource[] resources;
	
	public MergedResource(ResourceLocation location, List<Resource> resources)
	{
		this(location, resources.toArray(new Resource[resources.size()]));
	}
	
	public MergedResource(ResourceLocation location, Resource... resources)
	{
		super(location);
		
		this.resources = resources;
	}
	
	@Override
	public String getContentType()
	{
		return resources[0].getContentType();
	}

	@Override
	public int getContentLength()
	{
		int length = 0;
		for(Resource r : resources)
		{
			length += r.getContentLength();
		}
		
		return length;
	}

	@Override
	public String getContentEncoding()
	{
		return null;
	}

	@Override
	public long getLastModified()
	{
		long last = 0;
		for(Resource r : resources)
		{
			last = Math.max(r.getLastModified(), last);
		}
		
		return last;
	}

	@Override
	public InputStream openStream()
		throws IOException
	{
		return new MergedInputStream(resources);
	}
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "{location=" + getLocation() + ", resources=" + Arrays.toString(resources) + "}";
	}
	
	public Resource[] getResources()
	{
		return resources;
	}

	/**
	 * Input stream that merges several resources into one.
	 * 
	 * @author Andreas Holstenson
	 *
	 */
	// TODO: Add reading for byte arrays
	public static class MergedInputStream
		extends InputStream
	{
		private final Resource[] resources;
		private int currentResource;
		private InputStream currentStream;
		
		public MergedInputStream(Resource[] resources)
		{
			this.resources = resources;
			
			currentResource = -1;
		}
		
		/**
		 * Open the next resource for reading.
		 * 
		 * @return
		 * @throws IOException 
		 */
		private boolean openNext()
			throws IOException
		{
			currentResource++;
			if(currentResource >= resources.length)
			{
				// No more resources to try
				return false;
			}
			
			Resource resource = resources[currentResource];
			currentStream = resource.openStream();
			
			return true;
		}

		@Override
		public int read()
			throws IOException
		{
			if(currentResource == -1)
			{
				if(false == openNext())
				{
					return -1;
				}
			}
			
			while(true)
			{
				int read = currentStream.read();
				if(read == -1)
				{
					// Check if we can open another stream, and if so read again
					if(openNext()) continue;
					
					return -1;
				}
				
				return read;
			}
		}
		
	}
}
