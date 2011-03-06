package se.l4.dust.jaxrs.internal.asset;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;

import org.jdom.Namespace;

import com.google.inject.Inject;

import se.l4.dust.Dust;
import se.l4.dust.api.NamespaceManager;
import se.l4.dust.api.TemplateException;
import se.l4.dust.api.asset.AssetSource;
import se.l4.dust.api.resource.Resource;
import se.l4.dust.api.resource.UrlResource;
import se.l4.dust.jaxrs.annotation.ContextScoped;

@ContextScoped
public class ContextAssetSource
	implements AssetSource
{
	private final ServletContext ctx;

	private final NamespaceManager namespaces;

	@Inject
	public ContextAssetSource(ServletContext ctx, NamespaceManager namespaces)
	{
		this.ctx = ctx;
		this.namespaces = namespaces;
	}
	
	public Resource locate(Namespace ns, String pathToFile)
		throws IOException
	{
		if(Dust.CONTEXT_NAMESPACE.equals(ns))
		{
			if(false == namespaces.isBound(ns))
			{
				throw new TemplateException("Trying to use assets from the webapp context, but the namespace dust:context has not been bound");
			}
			
			try
			{
				URL url = ctx.getResource("/" + pathToFile);
				
				return url == null ? null : new UrlResource(url) ;
			}
			catch(MalformedURLException e)
			{
			}
		}
		
		return null;
	}

}
