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

}
