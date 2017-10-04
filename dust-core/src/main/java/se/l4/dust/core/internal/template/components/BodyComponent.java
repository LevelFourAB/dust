package se.l4.dust.core.internal.template.components;

import java.io.IOException;

import se.l4.dust.api.template.Emittable;
import se.l4.dust.api.template.TemplateEmitter;
import se.l4.dust.api.template.TemplateOutputStream;
import se.l4.dust.api.template.dom.Attribute;
import se.l4.dust.api.template.fragment.FragmentEncounter;
import se.l4.dust.api.template.fragment.TemplateFragment;
import se.l4.dust.core.internal.template.TemplateEmitterImpl;

public class BodyComponent
	implements TemplateFragment
{
	public BodyComponent()
	{
	}

	@Override
	public void build(FragmentEncounter encounter)
	{
		Attribute<String> param = encounter.getAttribute("id", String.class);
		if(param == null)
		{
			encounter.replaceWith(new SimpleBodyEmitter());
		}
		else
		{
			encounter.replaceWith(new ParameterEmitter(param));
		}
	}

	private static class SimpleBodyEmitter
		implements Emittable
	{
		public SimpleBodyEmitter()
		{
		}

		@Override
		public void emit(TemplateEmitter emitter, TemplateOutputStream output)
			throws IOException
		{
			TemplateEmitterImpl emitterImpl = (TemplateEmitterImpl) emitter;
			Emittable e = emitterImpl.getCurrentComponent();
			if(e == null) return;

			emitter.emit(e);
		}
	}

	private static class ParameterEmitter
		implements Emittable
	{
		private final Attribute<String> name;

		public ParameterEmitter(Attribute<String> name)
		{
			this.name = name;
		}

		@Override
		public void emit(TemplateEmitter emitter, TemplateOutputStream output)
			throws IOException
		{
			String actualName = name.get(emitter.getContext(), emitter.getObject());
			Emittable param = emitter.getParameter(actualName);
			if(param != null)
			{
				emitter.emit(param);
			}
		}
	}

}
