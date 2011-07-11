package se.l4.dust.core.internal.asset;

import java.io.IOException;
import java.net.URL;

import org.jdom.Namespace;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import se.l4.dust.api.NamespaceManager;
import se.l4.dust.api.asset.AssetSource;
import se.l4.dust.api.resource.Resource;
import se.l4.dust.api.resource.UrlResource;

/**
 * Asset source that resolves from the classpath via the use of 
 * {@link NamespaceManager}.
 * 
 * @author Andreas Holstenson
 *
 */
@Singleton
public class ClasspathAssetSource
	implements AssetSource
{
	private final NamespaceManager manager;

	@Inject
	public ClasspathAssetSource(NamespaceManager manager)
	{
		this.manager = manager;
	}

	public Resource locate(String ns, String path)
		throws IOException
	{
		URL url = manager.getResource(Namespace.getNamespace(ns), path);
		return url == null? null : new UrlResource(url);
	}

}
