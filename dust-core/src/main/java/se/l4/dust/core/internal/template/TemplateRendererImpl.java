package se.l4.dust.core.internal.template;

import java.net.URL;

import org.jdom.Document;
import org.jdom.JDOMException;

import com.google.inject.Inject;

import se.l4.dust.api.template.TemplateRenderer;
import se.l4.dust.core.internal.template.dom.TemplateEmitter;
import se.l4.dust.core.template.TemplateCache;

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
	
	public Document render(URL template, Object data)
		throws JDOMException
	{
		return emitter.process(
			cache.getTemplate(template), 
			data
		);
	}
}
