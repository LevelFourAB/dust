package se.l4.dust.api.discovery;

import se.l4.dust.api.Namespace;

public interface DiscoveryHandler
{
	void handle(Namespace ns, DiscoveryEncounter encounter);
}
