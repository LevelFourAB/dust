package se.l4.dust.api.template;

import org.jdom.Content;

/**
 * Extension to {@link Content} for template rendering.
 * 
 * @author Andreas Holstenson
 *
 */
public abstract class PropertyContent
	extends Content
{
	
	@Override
	public String getValue()
	{
		return null;
	}
	
	public abstract Object getValue(RenderingContext ctx, Object root);
	
	public abstract void setValue(RenderingContext ctx, Object root, Object data);
}
