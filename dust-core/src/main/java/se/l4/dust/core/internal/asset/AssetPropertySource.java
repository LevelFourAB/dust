package se.l4.dust.core.internal.asset;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import se.l4.dust.api.NamespaceManager;
import se.l4.dust.api.TemplateException;
import se.l4.dust.api.asset.Asset;
import se.l4.dust.api.asset.AssetManager;
import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.api.template.dom.DynamicContent;
import se.l4.dust.api.template.spi.PropertySource;
import se.l4.dust.api.template.spi.TemplateInfo;

import com.google.inject.Inject;
import com.google.inject.Stage;

/**
 * Property source for binding assets for use in templates.
 * 
 * @author andreas
 *
 */
public class AssetPropertySource
	implements PropertySource
{
	private static final Pattern pattern = Pattern.compile("([a-zA-Z0-9]+):(.+)");
	
	private final AssetManager manager;
	private final Stage stage;
	
	@Inject
	public AssetPropertySource(
			Stage stage,
			AssetManager manager)
	{
		this.stage = stage;
		this.manager = manager;
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
		
		return new Content(manager, stage == Stage.PRODUCTION, uri, path);
	}

	private static class Content
		extends DynamicContent
	{
		private final AssetManager manager;
		private final boolean production;
		private final String namespace;
		private final String path;

		public Content(AssetManager manager, boolean production, String namespace, String path)
		{
			this.manager = manager;
			this.production = production;
			this.namespace = namespace;
			this.path = path;
		}
		
		@Override
		public Class<?> getValueType()
		{
			return String.class;
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
			throw new UnsupportedOperationException();
		}
		
		public se.l4.dust.api.template.dom.Content doCopy()
		{
			return new Content(manager, production, namespace, path);
		}
	}
	
	/**
	 * Content implementation that will not resolve assets with the
	 * {@link AssetManager} on each call.
	 * 
	 * @author Andreas Holstenson
	 *
	 */
	private static class FixedContent
		extends DynamicContent
	{
		private final Asset asset;

		public FixedContent(Asset asset)
		{
			this.asset = asset;
		}
		
		@Override
		public Class<?> getValueType()
		{
			return String.class;
		}

		@Override
		public se.l4.dust.api.template.dom.Content doCopy()
		{
			return new FixedContent(asset);
		}

		@Override
		public Object getValue(RenderingContext ctx, Object root)
		{
			return ctx.resolveURI(asset);
		}

		@Override
		public void setValue(RenderingContext ctx, Object root, Object data)
		{
			throw new UnsupportedOperationException();
		}
	}
}
