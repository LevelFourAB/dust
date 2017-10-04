package se.l4.dust.api.asset;

public interface AssetPipelineBuilder
{
	AssetPipelineBuilder add(AssetProcessor processor);

	AssetPipeline build();
}
