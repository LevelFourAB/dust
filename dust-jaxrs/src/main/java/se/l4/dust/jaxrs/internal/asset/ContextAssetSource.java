package se.l4.dust.jaxrs.internal.asset;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;

import se.l4.dust.Dust;
import se.l4.dust.api.Namespaces;
import se.l4.dust.api.asset.AssetSource;
import se.l4.dust.api.resource.Resource;
import se.l4.dust.api.resource.UrlResource;
import se.l4.dust.api.template.TemplateException;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * Asset source that works on the {@link ServletContext}.
 * 
 * @author andreas
 *
 */
@Singleton
public class ContextAssetSource
	implements AssetSource
{
	private final Provider<ServletContext> ctx;

	private final Namespaces namespaces;

	@Inject
	public ContextAssetSource(Provider<ServletContext> ctx, Namespaces namespaces)
	{
		this.ctx = ctx;
		this.namespaces = namespaces;
	}
	
	public Resource locate(String ns, String pathToFile)
		throws IOException
	{
		if(Dust.CONTEXT_NAMESPACE_URI.equals(ns))
		{
			if(false == namespaces.isBound(ns))
			{
				throw new TemplateException("Trying to use assets from the webapp context, but the namespace " + Dust.CONTEXT_NAMESPACE_URI + " has not been bound");
			}
			
			try
			{
				URL url = ctx.get().getResource("/" + pathToFile);
				
				return url == null ? null : new UrlResource(url) ;
			}
			catch(MalformedURLException e)
			{
			}
		}
		
		return null;
	}

}
