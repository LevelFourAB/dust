package se.l4.dust.core.internal.template.mixins;

import java.io.IOException;

import se.l4.dust.api.conversion.NonGenericConversion;
import se.l4.dust.api.conversion.TypeConverter;
import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.api.template.dom.Attribute;
import se.l4.dust.api.template.mixin.ElementEncounter;
import se.l4.dust.api.template.mixin.ElementWrapper;
import se.l4.dust.api.template.mixin.MixinEncounter;
import se.l4.dust.api.template.mixin.TemplateMixin;
import se.l4.dust.core.internal.template.TemplateModule;

import com.google.inject.Inject;

public class RepeatMixin
	implements TemplateMixin
{
	private final TypeConverter converter;

	@Inject
	public RepeatMixin(TypeConverter converter)
	{
		this.converter = converter;
	}

	@Override
	public void element(MixinEncounter encounter)
	{
		final Attribute attribute = encounter.getAttribute(TemplateModule.COMMON, "repeat");
		final Attribute in = encounter.getAttribute(TemplateModule.COMMON, "in");
		if(in != null)
		{
			if(in.getValueType() != Integer.class && in.getValueType() != int.class)
			{
				encounter.error("Attribute in must be an integer");
			}
		}
		
		final NonGenericConversion<Object, Integer> conversion = converter.getDynamicConversion(attribute.getValueType(), Integer.class);
		encounter.wrap(new ElementWrapper()
		{
			@Override
			public void beforeElement(ElementEncounter encounter)
				throws IOException
			{
				RenderingContext ctx = encounter.getContext();
				Object object = encounter.getObject();
				
				Object value = attribute.getValue(ctx, object);
				Integer count = conversion.convert(value);
				for(int i=0, n=count; i<n; i++)
				{
					if(in != null) in.setValue(ctx, object, i);
					encounter.emit();
				}
			}
			
			@Override
			public void afterElement(ElementEncounter encounter)
			{
			}
		});
	}

}
