package se.l4.dust.api.template.dom;

import se.l4.dust.api.template.RenderingContext;

/**
 * Dynamic content that is determined on runtime.
 * 
 * @author Andreas Holstenson
 *
 */
public abstract class DynamicContent
	implements Content
{
	private Element parent;

	public Element getParent()
	{
		return parent;
	}

	public void setParent(Element element)
	{
		this.parent = element;
	}

	/**
	 * Get the value of this content determined via the current context.
	 * 
	 * @param ctx
	 * @param root
	 * @return
	 */
	public abstract Object getValue(RenderingContext ctx, Object root);
	
	/**
	 * Set the value of this content in the defined rendering context.
	 * 
	 * @param ctx
	 * @param root
	 * @param data
	 */
	public abstract void setValue(RenderingContext ctx, Object root, Object data);
}
