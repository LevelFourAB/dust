package se.l4.dust.api.asset;

import org.jdom.Namespace;

public interface AssetManager
{
	/**
	 * Attempt to locate the given asset by traversing all of the registered
	 * sources and returning the first match.
	 *  
	 * @param ns
	 * @param file
	 * @return
	 */
	Asset locate(Namespace ns, String file);
	
	/**
	 * Add a source of assets to the manager.
	 * 
	 * @param source
	 */
	void addSource(AssetSource source);

	/**
	 * Add a source of assets to the manager.
	 * 
	 * @param source
	 */
	void addSource(Class<? extends AssetSource> source);
	
	/**
	 * Get if the extension is protected and requires a checksum in the URL.
	 * Certain filetypes should be protected when served from e.g. the 
	 * classpath. The protected filetypes require that a checksum is calculated
	 * and included in the URL, example: Index.xml becomes Index.checksum.xml
	 * 
	 * <p>
	 * By default .xml and .class files are protected (templates and classes)
	 * 
	 * @param extension
	 * @return
	 */
	boolean isProtectedExtension(String extension);

	/**
	 * Add a protected extension, see {@link #isProtectedExtension(String)}
	 * for details.
	 * 
	 * @param extension
	 */
	void addProtectedExtension(String extension);

	/**
	 * Add access to a temporary asset in the given namespace. This is useful
	 * when assets are dynamically generated at runtime. Temporary assets can
	 * not overwrite existing assets and trying to do so will result in an
	 * error.
	 * 
	 * @param ns
	 * @param path
	 * @param resource
	 */
	void addTemporaryAsset(Namespace ns, String path, Resource resource);
	
	/**
	 * Define that assets in the given namespace matching the regular expression
	 * should be processed by the given classes before being used.
	 * 
	 * <p>
	 * Processing is only done once for every asset.
	 *  
	 * @param processor
	 */
	void processAssets(Namespace namespace, String filter, Class<? extends AssetProcessor>... processor);
}
