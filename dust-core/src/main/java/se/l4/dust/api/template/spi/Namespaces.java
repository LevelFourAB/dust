package se.l4.dust.api.template.spi;

import se.l4.dust.api.NamespaceManager;

/**
 * Information about namespaces when parsing templates.
 * 
 * @author Andreas Holstenson
 *
 */
public interface Namespaces
{
	/**
	 * Get a namespace URI by resolving the given prefix.
	 * 
	 * @param prefix
	 * @return
	 */
	NamespaceManager.Namespace getNamespaceByPrefix(String prefix);
}
