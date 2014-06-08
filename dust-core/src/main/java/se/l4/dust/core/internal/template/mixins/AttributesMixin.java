package se.l4.dust.core.internal.template.mixins;

import java.io.IOException;

import se.l4.dust.Dust;
import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.api.template.dom.Attribute;
import se.l4.dust.api.template.mixin.ElementEncounter;
import se.l4.dust.api.template.mixin.ElementWrapper;
import se.l4.dust.api.template.mixin.MixinEncounter;
import se.l4.dust.api.template.mixin.TemplateMixin;

public class AttributesMixin
	implements TemplateMixin
{

	@Override
	public void element(MixinEncounter encounter)
	{
		final String type = encounter.getAttribute(Dust.NAMESPACE_COMMON, "attributes").getStringValue();
		if(! type.equals("merge"))
		{
			encounter.error("`attributes` must have the value merge");
			return;
		}
		
		encounter.wrap(new ElementWrapper()
		{
			@Override
			public void beforeElement(ElementEncounter encounter)
				throws IOException
			{
				RenderingContext ctx = encounter.getContext();
				Object data = encounter.getObject();
				
				Attribute<String>[] extras = encounter.getContext().getValue("dust:extraAttributes");
				for(Attribute<String> a : extras)
				{
					encounter.pushAttribute(a.getName(), a.get(ctx, data));
				}
			}
			
			@Override
			public void afterElement(ElementEncounter encounter)
				throws IOException
			{
			}
		});
	}

}
