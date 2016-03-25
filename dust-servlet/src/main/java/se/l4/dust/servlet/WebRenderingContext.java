package se.l4.dust.servlet;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;

import com.google.inject.Inject;
import com.google.inject.Stage;

import se.l4.dust.api.Namespace;
import se.l4.dust.api.asset.Asset;
import se.l4.dust.api.resource.variant.ResourceVariant;
import se.l4.dust.api.template.DefaultRenderingContext;
import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.api.template.TemplateException;

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
	private final Stage stage;

	@Inject
	public WebRenderingContext(Stage stage)
	{
		this.stage = stage;
	}

	/**
	 * Setup this rendering request for the given servlet request.
	 * 
	 * @param request
	 */
	public void setup(HttpServletRequest request)
	{
		putValue(ResourceVariant.LOCALE, request.getLocale());
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
		Namespace ns = asset.getNamespace();
		if(ns != null)
		{
			String prefix = ns.getPrefix();
			String name = asset.getName();
			if(asset.isProtected())
			{
				int idx = name.lastIndexOf('.');
				String extension = name.substring(idx + 1);
				String checksum = asset.getChecksum();

				name = name.substring(0, idx) + "." + checksum + "." + extension; 
			}
			
			if(stage == Stage.PRODUCTION)
			{
				String version = ns.getVersion();
				
				return URI.create("/asset/" + prefix + "/" + name + "?" + version);
			}
			else
			{
				return URI.create("/asset/" + prefix + "/" + name);
			}
		}
		
		throw new TemplateException("Unable to resolve " + asset);
	}
}
