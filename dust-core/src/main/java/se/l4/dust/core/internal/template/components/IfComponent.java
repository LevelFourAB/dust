package se.l4.dust.core.internal.template.components;

import java.io.IOException;

import se.l4.dust.api.conversion.Conversion;
import se.l4.dust.api.conversion.NonGenericConversion;
import se.l4.dust.api.conversion.TypeConverter;
import se.l4.dust.api.template.Emittable;
import se.l4.dust.api.template.TemplateEmitter;
import se.l4.dust.api.template.dom.Content;
import se.l4.dust.api.template.dom.Element;
import se.l4.dust.api.template.dom.Element.Attribute;
import se.l4.dust.api.template.spi.FragmentEncounter;
import se.l4.dust.api.template.spi.TemplateFragment;
import se.l4.dust.api.template.spi.TemplateOutputStream;

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
		Element elseContents = encounter.findParameter("else");
		
		Class<?> type = test.getValueType();
		NonGenericConversion conversion;
		if(converter.canConvertBetween(type, Boolean.class))
		{
			conversion = converter.getConversion(type, Boolean.class);
		}
		else
		{
			conversion = converter.createWildcardConversionTo(Boolean.class);
		}
		
		encounter.replaceWith(new Component(test, conversion, elseContents, encounter.getBody()));
	}
	
	
	public static class Component
		implements Emittable
	{
		private final Attribute test;
		private final Conversion<Object, Boolean> conversion;
		private final Element elseContents;
		private final Content[] content;

		public Component(Attribute test, Conversion<Object, Boolean> conversion, Element elseContents, Content[] content)
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
				for(Content c : content)
				{
					emitter.emit(output, c);
				}
			}
			else if(elseContents != null)
			{
				// Render the else
				for(Content c : elseContents.getRawContents())
				{
					emitter.emit(output, c);
				}
			}
		}
	}
}
