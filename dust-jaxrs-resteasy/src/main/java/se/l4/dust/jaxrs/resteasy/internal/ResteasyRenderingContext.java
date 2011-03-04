package se.l4.dust.jaxrs.resteasy.internal;

import com.google.inject.Inject;

import se.l4.dust.api.NamespaceManager;
import se.l4.dust.jaxrs.spi.WebRenderingContext;

/**
 * Extension to {@link WebRenderingContext} for Resteasy specific 
 * injections.
 * 
 * @author Andreas Holstenson
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
