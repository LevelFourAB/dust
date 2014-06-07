package se.l4.dust.core.internal.template.components;

import java.io.IOException;

import se.l4.dust.api.template.Emittable;
import se.l4.dust.api.template.TemplateEmitter;
import se.l4.dust.api.template.TemplateOutputStream;
import se.l4.dust.api.template.Value;
import se.l4.dust.api.template.fragment.FragmentEncounter;
import se.l4.dust.api.template.fragment.TemplateFragment;

import com.google.inject.Inject;

public class IfComponent
	implements TemplateFragment
{
	@Inject
	public IfComponent()
	{
	}
	
	@Override
	public void build(FragmentEncounter encounter)
	{
		Value<Boolean> test = encounter.getAttribute("test", Boolean.class, true);
		Emittable elseContents = encounter.findParameter("else");
		
		encounter.replaceWith(new Component(test, elseContents, encounter.getBody()));
	}
	
	
	public static class Component
		implements Emittable
	{
		private final Value<Boolean> test;
		private final Emittable elseContents;
		private final Emittable[] content;

		public Component(Value<Boolean> test,
				Emittable elseContents,
				Emittable[] content)
		{
			this.test = test;
			this.content = content;
			this.elseContents = elseContents;
		}
		
		@Override
		public void emit(TemplateEmitter emitter, TemplateOutputStream output)
			throws IOException
		{
			Object data = emitter.getObject();
			Boolean value = test.get(emitter.getContext(), data);
			
			if(Boolean.TRUE.equals(value))
			{
				emitter.emit(content);
			}
			else if(elseContents != null)
			{
				emitter.emit(elseContents);
			}
		}
	}
}
