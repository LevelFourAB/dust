package se.l4.dust.api.asset;

import java.io.IOException;

import se.l4.dust.api.resource.Resource;

public interface AssetPipeline
{
	Resource process(Resource in)
		throws IOException;
}
