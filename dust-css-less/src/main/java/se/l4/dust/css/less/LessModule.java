package se.l4.dust.css.less;

import se.l4.crayon.CrayonModule;
import se.l4.crayon.annotation.Contribution;
import se.l4.crayon.annotation.Order;
import se.l4.dust.api.asset.Assets;
import se.l4.dust.core.CoreModule;

/**
 * Module that activates LESS CSS for all files ending with {@code .less}.
 * 
 * @author Andreas Holstenson
 *
 */
public class LessModule
	extends CrayonModule
{

	@Override
	protected void configure()
	{
		install(new CoreModule());
	}

	@Contribution(name="dust-css-less")
	@Order("before:dust-assets")
	public void contributeLess(Assets assets, LessProcessor processor)
	{
		assets.addExtensionProcessor("less", processor);
	}
}
