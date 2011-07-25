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
import se.l4.dust.api.template.spi.Namespaces;
import se.l4.dust.api.template.spi.PropertySource;

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
	
	public DynamicContent getPropertyContent(Namespaces namespaces, Class<?> context, String propertyExpression)
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
		
		Asset asset = manager.locate(uri, path);
		if(asset == null)
		{
			throw new TemplateException("No asset named " + path + " in " + uri);
		}
		
		return new Content(asset);
	}

	private static class Content
		extends DynamicContent
	{
		private final Asset asset;
		
		public Content(Asset asset)
		{
			this.asset = asset;
		}
		
		@Override
		public Object getValue(RenderingContext ctx, Object root)
		{
			return ctx.resolveURI(asset);
		}
		
		@Override
		public void setValue(RenderingContext ctx, Object root, Object data)
		{
		}
	}
}
