package se.l4.dust.core.internal.asset;

import java.io.IOException;
import java.io.InputStream;

import se.l4.dust.api.Context;
import se.l4.dust.api.asset.Asset;
import se.l4.dust.api.asset.AssetManager;
import se.l4.dust.api.resource.MergedResource;
import se.l4.dust.api.resource.Resource;


/**
 * Resource that will lookup merges assets together and will check with 
 * {@link AssetManager} for updates. 
 * 
 * @author Andreas Holstenson
 *
 */
public class MergedAssetResource
	implements Resource
{
	private final AssetManager manager;
	private final Asset[] assets;
	private final Context context;

	public MergedAssetResource(AssetManager manager, Context context, Asset... assets)
	{
		this.manager = manager;
		this.context = context;
		this.assets = assets;
	}
	
	public String getContentType()
	{
		return "unknown";
	}

	public int getContentLength()
	{
		int length = 0;
		for(Asset asset : assets)
		{
			asset = manager.locate(context, asset.getNamespace(), asset.getName());
			
			length += asset.getResource().getContentLength();
		}
		
		return length;
	}

	public String getContentEncoding()
	{
		return null;
	}

	public long getLastModified()
	{
		long last = 0;
		for(Asset asset : assets)
		{
			last = Math.max(asset.getResource().getLastModified(), last);
		}
		
		return last;
	}

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
