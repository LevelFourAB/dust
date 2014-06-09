package se.l4.dust.core.internal.template.components;

import java.io.IOException;

import se.l4.dust.api.template.Emittable;
import se.l4.dust.api.template.TemplateEmitter;
import se.l4.dust.api.template.TemplateOutputStream;
import se.l4.dust.api.template.dom.Attribute;
import se.l4.dust.api.template.fragment.FragmentEncounter;
import se.l4.dust.api.template.fragment.TemplateFragment;

public class RawComponent
	implements TemplateFragment
{
	public RawComponent()
	{
	}
	
	@Override
	public void build(FragmentEncounter encounter)
	{
		final Attribute<String> attribute = encounter.getAttribute("value", String.class, true);
		encounter.replaceWith(new Emittable()
		{
			@Override
			public void emit(TemplateEmitter emitter, TemplateOutputStream output)
				throws IOException
			{
				String value = attribute.get(emitter.getContext(), emitter.getObject());
				output.raw(value);
			}
		});
	}
}
