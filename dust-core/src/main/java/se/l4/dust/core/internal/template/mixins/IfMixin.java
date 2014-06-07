package se.l4.dust.core.internal.template.mixins;

import se.l4.dust.api.template.Value;
import se.l4.dust.api.template.mixin.ElementEncounter;
import se.l4.dust.api.template.mixin.ElementWrapper;
import se.l4.dust.api.template.mixin.MixinEncounter;
import se.l4.dust.api.template.mixin.TemplateMixin;
import se.l4.dust.core.internal.template.TemplateModule;

import com.google.inject.Inject;

public class IfMixin
	implements TemplateMixin
{
	@Inject
	public IfMixin()
	{
	}

	@Override
	public void element(MixinEncounter encounter)
	{
		final Value<Boolean> test = encounter.getAttribute(TemplateModule.COMMON, "if", Boolean.class);
		encounter.wrap(new ElementWrapper()
		{
			@Override
			public void beforeElement(ElementEncounter encounter)
			{
				Boolean value = test.get(encounter.getContext(), encounter.getObject());
				if(! Boolean.TRUE.equals(value))
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
