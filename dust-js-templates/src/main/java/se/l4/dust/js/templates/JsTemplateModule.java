package se.l4.dust.js.templates;

import se.l4.crayon.CrayonModule;
import se.l4.crayon.annotation.Contribution;
import se.l4.crayon.annotation.Order;
import se.l4.dust.api.asset.AssetManager;
import se.l4.dust.js.templates.internal.JavaScriptTemplatesImpl;

public class JsTemplateModule
	extends CrayonModule
{

	@Override
	protected void configure()
	{
		bind(JavaScriptTemplates.class).to(JavaScriptTemplatesImpl.class);
	}
	
	@Contribution
	@Order("before:internal-asset-sources")
	public void contributeAssetSource(AssetManager manager, JavaScriptTemplatesImpl js)
	{
		manager.addSource(js);
	}

}
