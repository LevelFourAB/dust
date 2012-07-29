package se.l4.dust.core.internal.template;

import se.l4.dust.api.template.dom.Element.Attribute;
import se.l4.dust.api.template.mixin.ElementEncounter;
import se.l4.dust.api.template.mixin.ElementWrapper;
import se.l4.dust.api.template.mixin.MixinEncounter;
import se.l4.dust.api.template.mixin.TemplateMixin;

public class IfMixin
	implements TemplateMixin
{
	public IfMixin()
	{
	}

	@Override
	public void element(MixinEncounter encounter)
	{
		final Attribute attribute = encounter.getAttribute(TemplateModule.COMMON, "if");
		encounter.wrap(new ElementWrapper()
		{
			@Override
			public void beforeElement(ElementEncounter encounter)
			{
				Object value = attribute.getValue(encounter.getContext(), encounter.getObject());
				if(Boolean.FALSE.equals(value))
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
