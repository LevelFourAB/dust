package se.l4.dust.jaxrs.spi;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.google.inject.Inject;

import se.l4.dust.api.NamespaceManager;
import se.l4.dust.api.TemplateException;
import se.l4.dust.api.asset.Asset;
import se.l4.dust.api.conversion.TypeConverter;
import se.l4.dust.api.template.DefaultRenderingContext;
import se.l4.dust.api.template.RenderingContext;

/**
 * Implementation of {@link RenderingContext} that resolves assets correctly
 * for JAX-RS.
 * 
 * @author Andreas Holstenson
 *
 */
public class WebRenderingContext
	extends DefaultRenderingContext
{
	@Inject
	public WebRenderingContext(NamespaceManager namespaceManager, TypeConverter converter)
	{
		super(namespaceManager, converter);
	}
	
	@Override
	public URI resolveURI(Object object)
	{
		if(object instanceof Asset)
		{
			return resolveAsset((Asset) object);
		}
		
		return null;
	}
	
	private URI resolveAsset(Asset asset)
	{
		URI uri = null;
		String ns = asset.getNamespace();
		if(ns != null)
		{
			NamespaceManager.Namespace nns = namespaceManager.getNamespaceByURI(ns);
			if(nns == null)
			{
				throw new RuntimeException("Namespace " + ns + " is not bound to NamespaceManager");
			}
			
			String prefix = nns.getPrefix();
			String name = asset.getName();
			if(asset.isProtected())
			{
				int idx = name.lastIndexOf('.');
				String extension = name.substring(idx + 1);
				String checksum = asset.getChecksum();

				name = name.substring(0, idx) + "." + checksum + "." + extension; 
			}
			
			String version = nns.getVersion();
			
			UriBuilder builder = UriBuilder.fromPath("/asset/{ns}/{version}")
				.path(name);
			
			return builder.build(prefix, version);
		}
		
		throw new TemplateException("Unable to resolve " + asset);
	}
}
