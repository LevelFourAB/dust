package se.l4.dust.core.internal.template.components;

import java.io.IOException;

import se.l4.dust.api.conversion.Conversion;
import se.l4.dust.api.conversion.NonGenericConversion;
import se.l4.dust.api.conversion.TypeConverter;
import se.l4.dust.api.template.Emittable;
import se.l4.dust.api.template.TemplateEmitter;
import se.l4.dust.api.template.TemplateOutputStream;
import se.l4.dust.api.template.dom.Attribute;
import se.l4.dust.api.template.fragment.FragmentEncounter;
import se.l4.dust.api.template.fragment.TemplateFragment;

import com.google.inject.Inject;

public class IfComponent
	implements TemplateFragment
{
	private TypeConverter converter;


	@Inject
	public IfComponent(TypeConverter converter)
	{
		this.converter = converter;
	}
	
	@Override
	public void build(FragmentEncounter encounter)
	{
		Attribute test = encounter.getAttribute("test");
		Emittable elseContents = encounter.findParameter("else");
		
		NonGenericConversion<Object, Boolean> conversion = converter.getDynamicConversion(test.getValueType(), Boolean.class);
		
		encounter.replaceWith(new Component(test, conversion, elseContents, encounter.getBody()));
	}
	
	
	public static class Component
		implements Emittable
	{
		private final Attribute test;
		private final Conversion<Object, Boolean> conversion;
		private final Emittable elseContents;
		private final Emittable[] content;

		public Component(Attribute test,
				Conversion<Object, Boolean> conversion,
				Emittable elseContents,
				Emittable[] content)
		{
			this.test = test;
			this.conversion = conversion;
			this.content = content;
			this.elseContents = elseContents;
		}
		
		@Override
		public void emit(TemplateEmitter emitter, TemplateOutputStream output)
			throws IOException
		{
			Object data = emitter.getObject();
			Object value = test.getValue(emitter.getContext(), data);
			Boolean bool = conversion.convert(value);
			
			if(Boolean.TRUE.equals(bool))
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
