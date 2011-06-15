package se.l4.dust.api.asset;

import org.jdom.Namespace;

import se.l4.dust.api.NamespaceManager;
import se.l4.dust.api.resource.Resource;

/**
 * Asset management, helps with locating and registering assets. Assets are
 * files that are usually located within the classpath of a project. Assets
 * are always bound to a namespace, given a namespace and a file name the
 * manager looks through registered {@link AssetSource}s for the given resource.
 * 
 * <h2>Classpath source</h2>
 * By default an {@link AssetSource} based on the classpath is registered, this
 * source will lookup the package name for a given namespace (via 
 * {@link NamespaceManager}) and will then perform a lookup of the file
 * equivalent to classing {@link Package#getResource(String)} on a class located
 * in the package.
 * 
 * <h2>Usage in templates</h2>
 * If assets are used in a template the template must define their namespace
 * as a xmlns-attribute. This is usually done on the top-level element of the
 * template, but can be done anywhere where the asset usage is within scope.
 * To resolve an asset to a string a property expansion is used as follows
 * ${asset:namespace:path/to/file}. This property will be replaced with a URL
 * that points to the given asset.
 * 
 * <h3>Example usage</h3>
 * <pre>
 * 	&lt;html xmlns:e="http://example.org"&gt;
 * 		&lt;head&gt;
 * 			&lt;link href="${asset:e:default.css}" rel="stylesheet" type="text/css"/&gt;
 * 		&lt;/head&gt;
 * 	&lt;/html&gt;
 * </pre>
 * 
 * <h2>Namespace prefixes and URLs</h2>
 * Usually one should bind namespaces containing a prefix (see 
 * {@link Namespace#getPrefix()}) as it will be used when serving an asset
 * over HTTP. The URL will normally be {@code /asset/prefix/path/to/file} where
 * prefix was defined when binding in {@link NamespaceManager}. If no prefix
 * has been set for namespace no assets from that namespace can be served.
 * 
 * @author Andreas Holstenson
 *
 */
public interface AssetManager
{
	/**
	 * Builder for merged assets.
	 * 
	 * @author Andreas Holstenson
	 *
	 */
	interface AssetBuilder
	{
		/**
		 * Add the specified asset to this builder, assuming the same
		 * namespace as the built asset. See {@link #add(Namespace, String)}
		 * for details.
		 * 
		 * @param ns
		 * @param pathToFile
		 * @return
		 */
		AssetBuilder add(String pathToFile);
		
		/**
		 * Add the specified asset to this builder. Each asset added will be
		 * treated as a regular asset and transformed before it is combined
		 * into a new asset.
		 * 
		 * @param ns
		 * @param pathToFile
		 * @return
		 */
		AssetBuilder add(Namespace ns, String pathToFile);
		
		/**
		 * Indicate that the built asset should be processed.
		 * 
		 * @return
		 */
		AssetBuilder process(Class<? extends AssetProcessor> processor, Object... args);
		
		/**
		 * Create the asset.
		 */
		void create();
	}
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
	 * when assets are dynamically generated at runtime.
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
	 * Examples:
	 * <pre>
	 * // To process all CSS files in the given namespace
	 * processAssets(ns, ".*\\.css", CssProcessor.class);
	 * 
	 * // To process a specific file
	 * processAssets(ns, "logo.png", ImageScaler.class);
	 * </pre>
	 * 
	 * <p>
	 * Processing is only done once for every asset.
	 *  
	 * @param processor
	 */
	void processAssets(Namespace namespace, String filter, Class<? extends AssetProcessor> processor);
	
	/**
	 * Define that assets in the given namespace matching the regular expression
	 * should be processed by the given classes before being used.
	 * 
	 * <p>
	 * Examples:
	 * <pre>
	 * // Pass two arguments to the processor
	 * processAssets(ns, "logo.png", ImageScaler.class, 100, 200);
	 * </pre>
	 * 
	 * <p>
	 * Processing is only done once for every asset.
	 *  
	 * @param processor
	 */
	void processAssets(Namespace namespace, String filter, Class<? extends AssetProcessor> processor, Object... arguments);
	
	/**
	 * Start building a custom combined asset. Combined assets are special in
	 * that they combine several files into a single one. This can be used
	 * to combined CSS or JavaScript files at runtime to reduce the number
	 * of HTTP-requests required.
	 * 
	 * @param namespace
	 * @param pathToFile
	 * @return
	 */
	AssetBuilder addAsset(Namespace namespace, String pathToFile);
}
