package se.l4.dust.core.internal.asset;

import java.net.URL;

import org.jdom.Namespace;

import se.l4.dust.api.NamespaceManager;
import se.l4.dust.api.asset.AssetSource;

import com.google.inject.Inject;

/**
 * Asset source that resolves from the classpath via the use of 
 * {@link NamespaceManager}.
 * 
 * @author Andreas Holstenson
 *
 */
public class ClasspathAssetSource
	implements AssetSource
{
	private final NamespaceManager manager;

	@Inject
	public ClasspathAssetSource(NamespaceManager manager)
	{
		this.manager = manager;
	}

	public URL locate(Namespace ns, String path)
	{
		return manager.getResource(ns, path);
	}

}
