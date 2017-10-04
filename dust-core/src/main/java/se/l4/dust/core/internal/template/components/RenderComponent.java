package se.l4.dust.core.internal.template.components;

import java.io.IOException;

import se.l4.dust.api.template.Emittable;
import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.api.template.TemplateCache;
import se.l4.dust.api.template.TemplateEmitter;
import se.l4.dust.api.template.TemplateOutputStream;
import se.l4.dust.api.template.dom.Attribute;
import se.l4.dust.api.template.dom.DocType;
import se.l4.dust.api.template.dom.ParsedTemplate;
import se.l4.dust.api.template.fragment.FragmentEncounter;
import se.l4.dust.api.template.fragment.TemplateFragment;
import se.l4.dust.core.internal.template.TemplateEmitterImpl;

import com.google.inject.Inject;

public class RenderComponent
	implements TemplateFragment
{
	private final TemplateCache cache;

	@Inject
	public RenderComponent(TemplateCache cache)
	{
		this.cache = cache;
	}

	@Override
	public void build(FragmentEncounter encounter)
	{
		final Attribute<?> attr = encounter.getAttribute("object", true);
		encounter.replaceWith(new Emittable()
		{
			@Override
			public void emit(TemplateEmitter emitter_, TemplateOutputStream output)
				throws IOException
			{
				TemplateEmitterImpl emitter = (TemplateEmitterImpl) emitter_;
				RenderingContext ctx = emitter.getContext();

				Object root = attr.get(ctx, emitter.getCurrentData());
				if(root instanceof Emittable)
				{
					emitter.emit((Emittable) root);
					return;
				}

				// Process the template of the component
				ParsedTemplate template = cache.getTemplate(ctx, root.getClass());

				// Switch to new context
				Object current = emitter.getCurrentData();
				Integer old = emitter.switchData(template.getRawId(), root);

				Integer oldComponent = emitter.switchComponent(template.getRawId(), null);

				DocType docType = template.getDocType();
				if(docType != null)
				{
					output.docType(docType.getName(), docType.getPublicId(), docType.getSystemId());
				}

				Emittable templateRoot = template.getRoot();
				emitter.emit(templateRoot);

				// Switch context back
				emitter.switchData(old, current);
				emitter.switchComponent(oldComponent);
			}
		});
	}
}
