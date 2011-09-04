package se.l4.dust.api.discovery;

/**
 * Factory for discovery functions.
 * 
 * @author Andreas Holstenson
 *
 */
public interface DiscoveryFactory
{
	/**
	 * Get a class discovery suitable for finding classes within the given
	 * namespace.
	 * 
	 * @param ns
	 * @return
	 */
	ClassDiscovery get(String pkg);

	/**
	 * Get an empty discovery that can't find any implementations.
	 * 
	 * @return
	 */
	ClassDiscovery emtpy();
	
	/**
	 * Add a top level class discovery to this factory.
	 * 
	 * @param cd
	 */
	void addTopLevel(ClassDiscovery cd);
}
