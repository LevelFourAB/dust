package se.l4.dust.core.internal.discovery;

import se.l4.dust.api.discovery.DiscoveryFactory;

import com.google.inject.AbstractModule;

/**
 * Module for discovery functions.
 * 
 * @author Andreas Holstenson
 *
 */
public class DiscoveryModule
	extends AbstractModule
{

	@Override
	protected void configure()
	{
		bind(DiscoveryFactory.class).to(DiscoveryFactoryImpl.class);
	}

}
