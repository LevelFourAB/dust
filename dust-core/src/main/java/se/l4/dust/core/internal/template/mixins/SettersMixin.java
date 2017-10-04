package se.l4.dust.core.internal.template.mixins;

import java.io.IOException;
import java.util.List;

import com.google.common.collect.Lists;

import se.l4.dust.Dust;
import se.l4.dust.api.Context;
import se.l4.dust.api.Value;
import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.api.template.dom.Attribute;
import se.l4.dust.api.template.mixin.ElementEncounter;
import se.l4.dust.api.template.mixin.ElementWrapper;
import se.l4.dust.api.template.mixin.MixinEncounter;
import se.l4.dust.api.template.mixin.TemplateMixin;

public class SettersMixin
	implements TemplateMixin
{
	@Override
	public void element(MixinEncounter encounter)
	{
		List<ValuePair<?>> setterPairs = Lists.newArrayList();
		for(Attribute<?> attr : encounter.getAttributes(Dust.NAMESPACE_SETTERS))
		{
			String name = attr.getName();
			Value<?> setter = encounter.parseExpression(name);
			if(! setter.supportsSet())
			{
				encounter.raiseError("The attribute " + name + " must exist and be settable");
			}

			Attribute<?> getter = encounter.getAttribute(Dust.NAMESPACE_SETTERS, name, setter.getType());

			setterPairs.add(new ValuePair(setter, getter));
		}

		final ValuePair<?>[] setters = setterPairs.toArray(new ValuePair[setterPairs.size()]);
		encounter.wrap(new ElementWrapper()
		{
			@Override
			public void beforeElement(ElementEncounter encounter)
				throws IOException
			{
				RenderingContext context = encounter.getContext();
				Object data = encounter.getObject();
				for(ValuePair<?> p : setters)
				{
					p.set(context, data);
				}
			}

			@Override
			public void afterElement(ElementEncounter encounter)
				throws IOException
			{
			}
		});
	}

	private static class ValuePair<T>
	{
		private final Value<T> setter;
		private final Value<? extends T> getter;

		public ValuePair(Value<T> setter, Value<? extends T> getter)
		{
			this.setter = setter;
			this.getter = getter;
		}

		public void set(Context ctx, Object data)
		{
			T value = getter.get(ctx, data);
			setter.set(ctx, data, value);
		}
	}
}
