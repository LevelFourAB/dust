package se.l4.dust.api.template.spi;

import se.l4.dust.api.Namespace;

/**
 * Information tied to a template while it is being parsed.
 * 
 * @author Andreas Holstenson
 *
 */
public interface TemplateInfo
{
	/**
	 * Get the URL of template being parsed.
	 * 
	 * @return
	 */
	String getURL();
	
	/**
	 * Get a namespace URI by resolving the given prefix.
	 * 
	 * @param prefix
	 * @return
	 */
	Namespace getNamespaceByPrefix(String prefix);
}
