package se.l4.dust.core.internal.template;

import java.net.URL;

import org.jdom.JDOMException;

import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.api.template.TemplateCache;
import se.l4.dust.api.template.TemplateRenderer;
import se.l4.dust.core.internal.template.dom.TemplateEmitter;
import se.l4.dust.dom.Document;

import com.google.inject.Inject;

public class TemplateRendererImpl
	implements TemplateRenderer
{
	private final TemplateEmitter emitter;
	private final TemplateCache cache;

	@Inject
	public TemplateRendererImpl(TemplateEmitter emitter, TemplateCache cache)
	{
		this.emitter = emitter;
		this.cache = cache;
	}
	
	public Document render(RenderingContext ctx, Document template, Object data)
		throws JDOMException
	{
		return emitter.process(template, ctx, data);
	}
	
	public Document render(RenderingContext ctx, URL template, Object data)
		throws JDOMException
	{
		return emitter.process(
			cache.getTemplate(template), 
			ctx,
			data
		);
	}
}
