package se.l4.dust.core.internal.asset;

import java.io.IOException;
import java.net.URL;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import se.l4.dust.api.Namespace;
import se.l4.dust.api.Namespaces;
import se.l4.dust.api.asset.AssetSource;
import se.l4.dust.api.resource.Resource;
import se.l4.dust.api.resource.UrlResource;

/**
 * Asset source that resolves from the classpath via the use of 
 * {@link Namespaces}.
 * 
 * @author Andreas Holstenson
 *
 */
@Singleton
public class ClasspathAssetSource
	implements AssetSource
{
	private final Namespaces manager;

	@Inject
	public ClasspathAssetSource(Namespaces manager)
	{
		this.manager = manager;
	}

	public Resource locate(String ns, String path)
		throws IOException
	{
		Namespace namespace = manager.getNamespaceByURI(ns);
		URL url = namespace.getResource(path);
		return url == null? null : new UrlResource(url);
	}

}
