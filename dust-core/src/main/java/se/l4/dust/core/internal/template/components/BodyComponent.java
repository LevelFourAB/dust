package se.l4.dust.core.internal.template.components;

import java.io.IOException;

import se.l4.dust.api.template.Emittable;
import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.api.template.TemplateEmitter;
import se.l4.dust.api.template.dom.Content;
import se.l4.dust.api.template.dom.Element;
import se.l4.dust.api.template.spi.FragmentEncounter;
import se.l4.dust.api.template.spi.TemplateFragment;
import se.l4.dust.api.template.spi.TemplateOutputStream;
import se.l4.dust.core.internal.template.dom.TemplateEmitterImpl;

public class BodyComponent
	implements TemplateFragment
{
	public BodyComponent()
	{
	}
	
	@Override
	public void build(FragmentEncounter encounter)
	{
		Element.Attribute param = encounter.getAttribute("parameter");
		if(param == null)
		{
			encounter.replaceWith(new SimpleBodyEmitter());
		}
		else
		{
			String paramName = param.getStringValue();
			encounter.replaceWith(new ParameterEmitter(paramName));
		}
	}
	
	private static class SimpleBodyEmitter
		implements Emittable
	{
		public SimpleBodyEmitter()
		{
			// TODO Auto-generated constructor stub
		}
		
		@Override
		public void emit(TemplateEmitter emitter, TemplateOutputStream output)
			throws IOException
		{
			TemplateEmitterImpl emitterImpl = (TemplateEmitterImpl) emitter;
			Element e = emitterImpl.getCurrentComponent();
			if(e == null) return;
			
			emitter.emit(output, e);
		}
	}
	
	private static class ParameterEmitter
		implements Emittable
	{
		private final String name;

		public ParameterEmitter(String name)
		{
			this.name = name;
		}
		
		@Override
		public void emit(TemplateEmitter emitter, TemplateOutputStream output)
			throws IOException
		{
			TemplateEmitterImpl emitterImpl = (TemplateEmitterImpl) emitter;
			Element e = emitterImpl.getCurrentComponent();
			if(e == null) return;
			
			Element param = e.getParameter(name);
			if(param != null)
			{
				emitter.emit(output, e);
			}
		}
	}

}
