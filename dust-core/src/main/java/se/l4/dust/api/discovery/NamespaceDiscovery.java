package se.l4.dust.api.discovery;


public interface NamespaceDiscovery
{
	/**
	 * Perform a new discovery within all namespaces.
	 */
	void performDiscovery();

	/**
	 * Add a new handler that will run for every namespace.
	 *
	 * @param handler
	 */
	void addHandler(DiscoveryHandler handler);
}
