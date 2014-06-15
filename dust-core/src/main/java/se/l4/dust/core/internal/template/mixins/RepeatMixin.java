package se.l4.dust.core.internal.template.mixins;

import java.io.IOException;

import se.l4.dust.Dust;
import se.l4.dust.api.Value;
import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.api.template.mixin.ElementEncounter;
import se.l4.dust.api.template.mixin.ElementWrapper;
import se.l4.dust.api.template.mixin.MixinEncounter;
import se.l4.dust.api.template.mixin.TemplateMixin;

import com.google.inject.Inject;

public class RepeatMixin
	implements TemplateMixin
{
	@Inject
	public RepeatMixin()
	{
	}

	@Override
	public void element(MixinEncounter encounter)
	{
		final Value<Integer> attribute = encounter.getAttribute(Dust.NAMESPACE_COMMON, "repeat", Integer.class);
		final Value<Integer> in = encounter.getAttribute(Dust.NAMESPACE_COMMON, "in", Integer.class);
		if(in != null && ! in.supportsSet())
		{
			encounter.raiseError("The attribute in must support setting of values");
		}
		
		encounter.wrap(new ElementWrapper()
		{
			@Override
			public void beforeElement(ElementEncounter encounter)
				throws IOException
			{
				RenderingContext ctx = encounter.getContext();
				Object object = encounter.getObject();
				
				Integer value = attribute.get(ctx, object);
				for(int i=0, n=value; i<n; i++)
				{
					if(in != null) in.set(ctx, object, i);
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
