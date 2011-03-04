package se.l4.dust.core.internal.template;

import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.core.internal.template.dom.TemplateEmitter;

/**
 * Context helper for templates, used by {@link TemplateEmitter}.
 * 
 * @author Andreas Holstenson
 *
 */
public class TemplateContext
{
	private static final ThreadLocal<RenderingContext> CONTEXTS = new ThreadLocal<RenderingContext>();
	
	public static void set(RenderingContext ctx)
	{
		CONTEXTS.set(ctx);
	}
	
	public static void clear()
	{
		CONTEXTS.remove();
	}

	public static RenderingContext get()
	{
		return CONTEXTS.get();
	}

}
