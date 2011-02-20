package se.l4.dust.jaxrs.spi;

import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.UriBuilder;

import org.jdom.Namespace;

import se.l4.dust.api.NamespaceManager;
import se.l4.dust.api.TemplateException;
import se.l4.dust.api.asset.Asset;
import se.l4.dust.api.template.TemplateContext;

import com.google.inject.Inject;

/**
 * Implementation of {@link TemplateContext} that resolves assets correctly
 * for JAX-RS.
 * 
 * @author Andreas Holstenson
 *
 */
public class DefaultRenderingContext
	implements TemplateContext
{
	private final NamespaceManager namespaceManager;
	private final Map<Object, Object> values;

	@Inject
	public DefaultRenderingContext(NamespaceManager namespaceManager)
	{
		this.namespaceManager = namespaceManager;
		values = new HashMap<Object, Object>();
	}
	
	public Object getValue(Object key)
	{
		return values.get(key);
	}
	
	public void putValue(Object key, Object value)
	{
		values.put(key, value);
	}
	
	public URI resolveURI(Object object)
	{
		if(object instanceof Asset)
		{
			return resolveAsset((Asset) object);
		}
		
		return null;
	}
	
	public Object resolveObject(Class<?> type, Annotation[] annotations)
	{
		return null;
	}
	
	private URI resolveAsset(Asset asset)
	{
		URI uri = null;
		Namespace ns = asset.getNamespace();
		if(ns != null)
		{
			Namespace nns = namespaceManager.getNamespaceByURI(ns.getURI());
			if(nns == null)
			{
				throw new RuntimeException("Namespace " + ns.getURI() + " is not bound to NamespaceManager");
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
			
			String version = namespaceManager.getVersion(nns);
			
			UriBuilder builder = UriBuilder.fromPath("/asset/{ns}/{version}")
				.path(name);
			
			return builder.build(prefix, version);
		}
		
		throw new TemplateException("Unable to resolve " + asset);
	}
}
