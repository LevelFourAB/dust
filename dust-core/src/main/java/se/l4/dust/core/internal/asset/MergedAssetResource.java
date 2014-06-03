package se.l4.dust.core.internal.asset;

import java.io.IOException;
import java.io.InputStream;

import se.l4.dust.api.Context;
import se.l4.dust.api.asset.Asset;
import se.l4.dust.api.asset.Assets;
import se.l4.dust.api.resource.AbstractResource;
import se.l4.dust.api.resource.MergedResource;
import se.l4.dust.api.resource.Resource;
import se.l4.dust.api.resource.ResourceLocation;


/**
 * Resource that will lookup merges assets together and will check with 
 * {@link Assets} for updates. 
 * 
 * @author Andreas Holstenson
 *
 */
public class MergedAssetResource
	extends AbstractResource
{
	private final Assets manager;
	private final Asset[] assets;
	private final Context context;

	public MergedAssetResource(ResourceLocation location, Assets manager, Context context, Asset... assets)
	{
		super(location);
		
		this.manager = manager;
		this.context = context;
		this.assets = assets;
	}
	
	@Override
	public String getContentType()
	{
		return "unknown";
	}

	@Override
	public int getContentLength()
	{
		int length = 0;
		for(Asset asset : assets)
		{
			asset = manager.locate(context, asset.getNamespace().getUri(), asset.getName());
			
			length += asset.getResource().getContentLength();
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
		for(Asset asset : assets)
		{
			last = Math.max(asset.getResource().getLastModified(), last);
		}
		
		return last;
	}

	@Override
	public InputStream openStream()
		throws IOException
	{
		Resource[] resources = new Resource[assets.length];
		for(int i=0, n=resources.length; i<n; i++)
		{
			resources[i] = assets[i].getResource();
		}
		
		return new MergedResource.MergedInputStream(resources);
	}
}
