package se.l4.dust.jaxrs.resteasy.internal;

import se.l4.dust.api.NamespaceManager;
import se.l4.dust.jaxrs.spi.DefaultRenderingContext;

import com.google.inject.Inject;

/**
 * Extension to {@link DefaultRenderingContext} for Resteasy specific 
 * injections.
 * 
 * @author andreas
 *
 */
public class ResteasyRenderingContext
	extends DefaultRenderingContext
{
	@Inject
	public ResteasyRenderingContext(NamespaceManager namespaceManager)
	{
		super(namespaceManager);
	}

}
