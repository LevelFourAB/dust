package se.l4.dust.js.coffeescript;

import se.l4.crayon.CrayonModule;
import se.l4.crayon.annotation.Contribution;
import se.l4.crayon.annotation.Order;
import se.l4.dust.api.asset.Assets;
import se.l4.dust.core.CoreModule;

/**
 * Module that activates CoffeeScript compilation for all files ending with 
 * {@code .coffee}.
 * 
 * @author Andreas Holstenson
 *
 */
public class CoffeeScriptModule
	extends CrayonModule
{

	@Override
	protected void configure()
	{
		install(new CoreModule());
	}

	@Contribution(name="dust-js-coffeescript")
	@Order("before:dust-assets")
	public void contributeProcessor(Assets assets, CoffeeScriptProcessor processor)
	{
		assets.addExtensionProcessor("coffee", processor);
	}
}
