package se.l4.dust.jaxrs.resteasy.internal;

import se.l4.dust.api.NamespaceManager;
import se.l4.dust.jaxrs.spi.WebRenderingContext;

import com.google.inject.Inject;

/**
 * Extension to {@link WebRenderingContext} for Resteasy specific 
 * injections.
 * 
 * @author andreas
 *
 */
public class ResteasyRenderingContext
	extends WebRenderingContext
{
	@Inject
	public ResteasyRenderingContext(NamespaceManager namespaceManager)
	{
		super(namespaceManager);
	}

}
