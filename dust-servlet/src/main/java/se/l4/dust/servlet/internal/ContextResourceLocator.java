package se.l4.dust.servlet.internal;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;

import se.l4.dust.Dust;
import se.l4.dust.api.Namespaces;
import se.l4.dust.api.resource.NamespaceLocation;
import se.l4.dust.api.resource.Resource;
import se.l4.dust.api.resource.ResourceLocator;
import se.l4.dust.api.resource.UrlResource;
import se.l4.dust.api.template.TemplateException;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Asset source that works on the {@link ServletContext}.
 *
 * @author andreas
 *
 */
public class ContextResourceLocator
	implements ResourceLocator
{
	private final Provider<ServletContext> ctx;

	private final Namespaces namespaces;

	@Inject
	public ContextResourceLocator(Provider<ServletContext> ctx, Namespaces namespaces)
	{
		this.ctx = ctx;
		this.namespaces = namespaces;
	}

	@Override
	public Resource locate(String ns, String pathToFile)
		throws IOException
	{
		if(Dust.NAMESPACE_CONTEXT.equals(ns))
		{
			if(false == namespaces.isBound(ns))
			{
				throw new TemplateException("Trying to use assets from the webapp context, but the namespace " + Dust.NAMESPACE_CONTEXT + " has not been bound");
			}

			try
			{
				URL url = ctx.get().getResource("/" + pathToFile);

				return url == null ? null : new UrlResource(new NamespaceLocation(namespaces.getNamespaceByURI(ns), pathToFile), url) ;
			}
			catch(MalformedURLException e)
			{
			}
		}

		return null;
	}

}
