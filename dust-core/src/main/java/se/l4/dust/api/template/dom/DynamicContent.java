package se.l4.dust.api.template.dom;

import java.io.IOException;

import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.api.template.TemplateEmitter;
import se.l4.dust.api.template.TemplateOutputStream;

/**
 * Dynamic content that is determined on runtime.
 * 
 * @author Andreas Holstenson
 *
 */
public abstract class DynamicContent
	extends AbstractContent
{
	public DynamicContent()
	{
	}
	
	@Override
	public void emit(TemplateEmitter emitter, TemplateOutputStream output)
		throws IOException
	{
		RenderingContext ctx = emitter.getContext();
		Object value = ctx.getDynamicValue(this, emitter.getObject());
		output.text(ctx.getStringValue(value));
	}
	
	/**
	 * Get the type that this content returns.
	 * 
	 * @return
	 */
	public abstract Class<?> getValueType();

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
	
	@Override
	public int getLine()
	{
		return line;
	}
	
	@Override
	public int getColumn()
	{
		return column;
	}
}
