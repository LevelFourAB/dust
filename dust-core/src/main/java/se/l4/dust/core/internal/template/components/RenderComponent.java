package se.l4.dust.core.internal.template.components;

import java.io.IOException;

import se.l4.dust.api.annotation.Template;
import se.l4.dust.api.conversion.TypeConverter;
import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.api.template.TemplateCache;
import se.l4.dust.api.template.dom.Content;
import se.l4.dust.api.template.dom.DocType;
import se.l4.dust.api.template.dom.Element;
import se.l4.dust.api.template.dom.ParsedTemplate;
import se.l4.dust.api.template.dom.WrappedElement;
import se.l4.dust.api.template.spi.TemplateOutputStream;
import se.l4.dust.core.internal.template.dom.Emitter;

import com.google.inject.Inject;

public class RenderComponent
	extends EmittableComponent
{
	private final TemplateCache cache;

	@Inject
	public RenderComponent(TemplateCache cache)
	{
		super("render", RenderComponent.class);
		
		this.cache = cache;
	}

	@Override
	public void emit(Emitter emitter, RenderingContext ctx, TemplateOutputStream out)
		throws IOException
	{
		Attribute attr = getAttribute("object");
		if(attr == null) return;
		
		Object root = attr.getValue(ctx, emitter.getCurrentData());
		
		// Process the template of the component 
		ParsedTemplate template = cache.getTemplate(ctx, root.getClass(), (Template) null);
		
		// Switch to new context
		Object current = emitter.getCurrentData();
		Integer old = emitter.switchData(template.getRawId(), root);
		
		Integer oldComponent = emitter.switchComponent(template.getRawId(), null);
		
		DocType docType = template.getDocType();
		if(docType != null)
		{
			out.docType(docType.getName(), docType.getPublicId(), docType.getSystemId());
		}
		
		Element templateRoot = template.getRoot();
		
		emitter.emit(out, templateRoot);
		
		// Switch context back
		emitter.switchData(old, current);
		emitter.switchComponent(oldComponent);
	}
}
