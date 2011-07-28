package se.l4.dust.core.internal.asset;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.inject.Inject;

import se.l4.dust.api.NamespaceManager;
import se.l4.dust.api.TemplateException;
import se.l4.dust.api.asset.Asset;
import se.l4.dust.api.asset.AssetManager;
import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.api.template.dom.DynamicContent;
import se.l4.dust.api.template.spi.PropertySource;
import se.l4.dust.api.template.spi.TemplateInfo;

/**
 * Property source for binding assets for use in templates.
 * 
 * @author andreas
 *
 */
public class AssetPropertySource
	implements PropertySource
{
	private Pattern pattern = Pattern.compile("([a-zA-Z0-9]+):(.+)");
	private final AssetManager manager;
	private final NamespaceManager namespaces;
	
	@Inject
	public AssetPropertySource(AssetManager manager, NamespaceManager namespaces)
	{
		this.manager = manager;
		this.namespaces = namespaces;
	}
	
	public DynamicContent getPropertyContent(TemplateInfo namespaces, Class<?> context, String propertyExpression)
	{
		Matcher matcher = pattern.matcher(propertyExpression);
		if(false == matcher.matches())
		{
			throw new TemplateException("No namespace present in " + propertyExpression
				+ "; Expected format to be ${asset:ns:path/to/file}, where ns is declared in the calling template"
			);
		}
		
		String prefix = matcher.group(1);
		String path = matcher.group(2);
		
		NamespaceManager.Namespace ns = namespaces.getNamespaceByPrefix(prefix);
		
		String uri = null;
		if(ns != null)
		{
			uri = ns.getUri();
		}
		
		if(uri == null)
		{
			throw new TemplateException("Namespace " + prefix 
				+ " not found for expression ${asset:"
				+ propertyExpression + "}"
			);
		}
		
		return new Content(manager, uri, path);
	}

	private static class Content
		extends DynamicContent
	{
		private final AssetManager manager;
		private final String namespace;
		private final String path;

		public Content(AssetManager manager, String namespace, String path)
		{
			this.manager = manager;
			this.namespace = namespace;
			this.path = path;
		}
		
		@Override
		public Object getValue(RenderingContext ctx, Object root)
		{
			Asset asset = manager.locate(ctx, namespace, path);
			if(asset == null)
			{
				throw new TemplateException("No asset named " + path + " in " + namespace);
			}
			
			return ctx.resolveURI(asset);
		}
		
		@Override
		public void setValue(RenderingContext ctx, Object root, Object data)
		{
		}
		
		public se.l4.dust.api.template.dom.Content copy()
		{
			return new Content(manager, namespace, path);
		}
	}
}
