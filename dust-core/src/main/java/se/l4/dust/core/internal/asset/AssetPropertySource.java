package se.l4.dust.core.internal.asset;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom.Namespace;

import se.l4.dust.api.asset.Asset;
import se.l4.dust.api.asset.AssetManager;
import se.l4.dust.api.template.PropertyContent;
import se.l4.dust.api.template.PropertySource;
import se.l4.dust.core.internal.template.dom.TemplateUtils;
import se.l4.dust.dom.Element;

import com.google.inject.Inject;

public class AssetPropertySource
	implements PropertySource
{
	private Pattern pattern = Pattern.compile("([a-zA-Z0-9]+):(.+)");
	private final AssetManager manager;
	
	@Inject
	public AssetPropertySource(AssetManager manager)
	{
		this.manager = manager;
	}
	
	public PropertyContent getPropertyContent(
			String propertyExpression,
			Element parent)
	{
		Matcher matcher = pattern.matcher(propertyExpression);
		if(false == matcher.matches())
		{
			TemplateUtils.throwException(parent, "No namespace present in " 
				+ propertyExpression 
				+ "; Expected format to be ${asset:ns:path/to/file}, where ns is declared in the calling template"
			);
		}
		
		String prefix = matcher.group(1);
		String path = matcher.group(2);
		
		Namespace ns = parent.getNamespace(prefix);
		if(ns == null)
		{
			TemplateUtils.throwException(parent, "Namespace " + prefix 
				+ " not found for expression ${asset:"
				+ propertyExpression + "}"
			);
		}
		
		Asset asset = manager.locate(ns, path);
		if(asset == null)
		{
			TemplateUtils.throwException(parent, "No asset named " + path + " in " + ns);
		}
		
		return new Content(asset);
	}

	private static class Content
		extends PropertyContent
	{
		private final Asset asset;
		
		public Content(Asset asset)
		{
			this.asset = asset;
		}
		
		@Override
		public Object getValue(Object root)
		{
			return asset;
		}
		
		@Override
		public void setValue(Object root, Object data)
		{
			// TODO Auto-generated method stub
			
		}
	}
}
