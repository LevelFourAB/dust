package se.l4.dust.core.internal.template.mixins;

import com.google.inject.Inject;

import se.l4.dust.api.conversion.NonGenericConversion;
import se.l4.dust.api.conversion.TypeConverter;
import se.l4.dust.api.template.dom.Attribute;
import se.l4.dust.api.template.mixin.ElementEncounter;
import se.l4.dust.api.template.mixin.ElementWrapper;
import se.l4.dust.api.template.mixin.MixinEncounter;
import se.l4.dust.api.template.mixin.TemplateMixin;
import se.l4.dust.core.internal.template.TemplateModule;

public class IfMixin
	implements TemplateMixin
{
	private final TypeConverter converter;

	@Inject
	public IfMixin(TypeConverter converter)
	{
		this.converter = converter;
	}

	@Override
	public void element(MixinEncounter encounter)
	{
		final Attribute attribute = encounter.getAttribute(TemplateModule.COMMON, "if");
		final NonGenericConversion<Object, Boolean> conversion = converter.getDynamicConversion(attribute.getValueType(), Boolean.class);
		encounter.wrap(new ElementWrapper()
		{
			@Override
			public void beforeElement(ElementEncounter encounter)
			{
				Object value = attribute.getValue(encounter.getContext(), encounter.getObject());
				Boolean bool = conversion.convert(value);
				if(! Boolean.TRUE.equals(bool))
				{
					encounter.skip();
				}
			}
			
			@Override
			public void afterElement(ElementEncounter encounter)
			{
			}
		});
	}

}
