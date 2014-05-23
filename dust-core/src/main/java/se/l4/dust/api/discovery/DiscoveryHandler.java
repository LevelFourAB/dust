package se.l4.dust.api.discovery;

import se.l4.dust.api.NamespaceManager;

public interface DiscoveryHandler
{
	void handle(NamespaceManager.Namespace ns, DiscoveryEncounter encounter);
}
