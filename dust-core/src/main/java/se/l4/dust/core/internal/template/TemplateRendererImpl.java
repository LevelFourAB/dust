package se.l4.dust.core.internal.template;

import java.io.IOException;
import java.net.URL;

import com.google.inject.Inject;

import se.l4.dust.api.annotation.Template;
import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.api.template.TemplateCache;
import se.l4.dust.api.template.TemplateRenderer;
import se.l4.dust.api.template.dom.ParsedTemplate;
import se.l4.dust.api.template.spi.TemplateOutputStream;
import se.l4.dust.core.internal.template.dom.Emitter;

public class TemplateRendererImpl
	implements TemplateRenderer
{
	private final Emitter emitter;
	private final TemplateCache cache;

	@Inject
	public TemplateRendererImpl(Emitter emitter, TemplateCache cache)
	{
		this.emitter = emitter;
		this.cache = cache;
	}
	
	public void render(RenderingContext ctx, ParsedTemplate template, Object data, TemplateOutputStream out)
		throws IOException
	{
		emitter.process(template, ctx, data, out);
	}
	
	public void render(RenderingContext ctx, Object data, TemplateOutputStream out)
		throws IOException
	{
		Class<?> c = data.getClass();
		if(false == c.isAnnotationPresent(Template.class))
		{
			throw new IllegalArgumentException("Object of type " + c + " does not have a @" + Template.class.getSimpleName() + " annotation");
		}
		
		ParsedTemplate tpl = cache.getTemplate(c, c.getAnnotation(Template.class));
		render(ctx, tpl, data, out);
	}
	
	public void render(RenderingContext ctx, Object data, URL template,
			TemplateOutputStream out)
		throws IOException
	{
		Class<?> c = data.getClass();
		
		ParsedTemplate tpl = cache.getTemplate(c, template);
		render(ctx, tpl, data, out);
	}
}
