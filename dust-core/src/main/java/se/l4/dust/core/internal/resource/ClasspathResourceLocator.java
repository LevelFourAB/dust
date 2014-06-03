package se.l4.dust.core.internal.resource;

import java.io.IOException;
import java.net.URL;

import se.l4.dust.api.Namespace;
import se.l4.dust.api.Namespaces;
import se.l4.dust.api.resource.NamespaceLocation;
import se.l4.dust.api.resource.Resource;
import se.l4.dust.api.resource.ResourceLocator;
import se.l4.dust.api.resource.UrlResource;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Asset source that resolves from the classpath via the use of 
 * {@link Namespaces}.
 * 
 * @author Andreas Holstenson
 *
 */
@Singleton
public class ClasspathResourceLocator
	implements ResourceLocator
{
	private final Namespaces manager;

	@Inject
	public ClasspathResourceLocator(Namespaces manager)
	{
		this.manager = manager;
	}

	@Override
	public Resource locate(String ns, String path)
		throws IOException
	{
		Namespace namespace = manager.getNamespaceByURI(ns);
		URL url = namespace.getClasspathResource(path);
		return url == null? null : new UrlResource(new NamespaceLocation(namespace, path), url);
	}

}
