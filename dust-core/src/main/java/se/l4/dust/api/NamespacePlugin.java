package se.l4.dust.api;

import com.google.inject.Injector;

public interface NamespacePlugin
{
	/**
	 * Perform registration for the given namespace.
	 * 
	 * @param ns
	 */
	void register(Injector injector, NamespaceManager.Namespace ns);
}
