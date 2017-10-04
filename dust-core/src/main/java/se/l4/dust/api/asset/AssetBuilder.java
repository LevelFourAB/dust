package se.l4.dust.api.asset;

import se.l4.dust.api.resource.ResourceLocation;

/**
 * Builder for merged assets.
 *
 * @author Andreas Holstenson
 *
 */
public interface AssetBuilder
{
	/**
	 * Add the specified asset to this builder, assuming the same
	 * namespace as the built asset.
	 *
	 * @param pathToFile
	 * @return
	 */
	AssetBuilder add(String pathToFile);

	/**
	 * Add the specified asset to this builder with a pipeline to run for
	 * the resource.
	 *
	 * @param pathToFile
	 * @param pipeline
	 * @return
	 */
	AssetBuilder add(String pathToFile, AssetPipeline pipeline);

	/**
	 *
	 * @param ns
	 * @param pathToFile
	 * @return
	 */
	AssetBuilder add(String ns, String pathToFile);


	/**
	 * Add the specified asset to this builder with a pipeline to run for
	 * the resource.
	 *
	 * @param ns
	 * @param pathToFile
	 * @return
	 */
	AssetBuilder add(String ns, String pathToFile, AssetPipeline pipeline);

	/**
	 * Add a resource to the asset.
	 *
	 * @param location
	 * @return
	 */
	AssetBuilder add(ResourceLocation location);

	/**
	 * Add a resource to the asset with a pipeline to run.
	 *
	 * @param location
	 * @param pipeline
	 * @return
	 */
	AssetBuilder add(ResourceLocation location, AssetPipeline pipeline);

	/**
	 * Indicate that the built asset should be processed with the specified
	 * processor.
	 *
	 * @param processor
	 * @return
	 */
	AssetBuilder process(AssetProcessor processor);

	/**
	 * Create the asset.
	 */
	void create();
}
