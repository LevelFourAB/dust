package se.l4.dust.core.internal.template.components;

import java.io.IOException;

import se.l4.dust.api.template.Emittable;
import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.api.template.TemplateEmitter;
import se.l4.dust.api.template.TemplateOutputStream;
import se.l4.dust.api.template.dom.Attribute;
import se.l4.dust.api.template.fragment.FragmentEncounter;
import se.l4.dust.api.template.fragment.TemplateFragment;
import se.l4.dust.core.internal.template.dom.TemplateEmitterImpl;

public class RawComponent
	implements TemplateFragment
{
	public RawComponent()
	{
	}
	
	@Override
	public void build(FragmentEncounter encounter)
	{
		final Attribute attribute = encounter.getAttribute("value");
		encounter.replaceWith(new Emittable()
		{
			@Override
			public void emit(TemplateEmitter emitter, TemplateOutputStream output)
				throws IOException
			{
				String value = attribute.getStringValue(emitter.getContext(), emitter.getObject());
				output.raw(value);
			}
		});
	}
}
