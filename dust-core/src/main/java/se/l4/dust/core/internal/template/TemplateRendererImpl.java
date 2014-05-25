package se.l4.dust.core.internal.template;

import java.io.IOException;
import java.net.URL;

import se.l4.dust.api.Context;
import se.l4.dust.api.Scopes;
import se.l4.dust.api.template.Component;
import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.api.template.Template;
import se.l4.dust.api.template.TemplateCache;
import se.l4.dust.api.template.TemplateRenderer;
import se.l4.dust.api.template.dom.ParsedTemplate;
import se.l4.dust.api.template.spi.TemplateOutputStream;
import se.l4.dust.core.internal.template.dom.TemplateEmitterImpl;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Renderer of templates, delegates most of its work to {@link TemplateEmitterImpl}.
 * 
 * @author Andreas Holstenson
 *
 */
@Singleton
public class TemplateRendererImpl
	implements TemplateRenderer
{
	private final TemplateCache cache;

	@Inject
	public TemplateRendererImpl(TemplateCache cache)
	{
		this.cache = cache;
	}
	
	public void render(RenderingContext ctx, ParsedTemplate template, Object data, TemplateOutputStream out)
		throws IOException
	{
		Context previous = Scopes.getActiveContext();
		Scopes.setActiveContext(ctx);
		try
		{
			TemplateEmitterImpl emitter = new TemplateEmitterImpl(template, ctx, data);
			emitter.process(out);
		}
		finally
		{
			Scopes.setActiveContext(previous);
		}
	}
	
	public void render(RenderingContext ctx, Object data, TemplateOutputStream out)
		throws IOException
	{
		Class<?> c = data.getClass();
		if(false == c.isAnnotationPresent(Template.class) && false == c.isAnnotationPresent(Component.class))
		{
			throw new IllegalArgumentException("Object of type " + c + " does not have a @" + Template.class.getSimpleName() + " annotation");
		}
		
		ParsedTemplate tpl = cache.getTemplate(ctx, c, c.getAnnotation(Template.class));
		render(ctx, tpl, data, out);
	}
	
	public void render(RenderingContext ctx, Object data, URL template,
			TemplateOutputStream out)
		throws IOException
	{
		Class<?> c = data.getClass();
		
		ParsedTemplate tpl = cache.getTemplate(ctx, c, template);
		render(ctx, tpl, data, out);
	}
}
