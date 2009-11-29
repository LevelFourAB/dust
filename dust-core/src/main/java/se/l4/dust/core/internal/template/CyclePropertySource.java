package se.l4.dust.core.internal.template;

import java.util.HashMap;
import java.util.Map;

import se.l4.dust.api.annotation.RequestScoped;
import se.l4.dust.api.template.PropertyContent;
import se.l4.dust.api.template.PropertySource;
import se.l4.dust.dom.Element;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class CyclePropertySource
	implements PropertySource
{
	private final Provider<CycleCounter> counter;

	@Inject
	public CyclePropertySource(Provider<CycleCounter> counter)
	{
		this.counter = counter;
	}
	
	public PropertyContent getPropertyContent(String propertyExpression, Element parent)
	{
		String[] parts = propertyExpression.split(",");
		
		return new Content(counter, parts);
	}

	public static class Content
		extends PropertyContent
	{
		private final Provider<CycleCounter> counter;
		private final String[] parts;

		public Content(Provider<CycleCounter> counter, String[] parts)
		{
			this.counter = counter;
			this.parts = parts;
		}
		
		@Override
		public Object getValue(Object root)
		{
			return counter.get().getValue(this, parts);
		}
		
		@Override
		public void setValue(Object root, Object data)
		{
			throw new UnsupportedOperationException("setValue can not be done on cycle bindings");
		}
	}
	
	@RequestScoped
	public static class CycleCounter
	{
		private final Map<Object, MutableInt> counts;
		
		public CycleCounter()
		{
			counts = new HashMap<Object, MutableInt>();
		}
		
		public Object getValue(PropertyContent content, String[] values)
		{
			MutableInt count = counts.get(content);
			if(count == null)
			{
				count = new MutableInt();
				counts.put(content, count);
			}
			
			String value = values[count.value];
			
			count.value++;
			if(count.value >= values.length)
			{
				count.value = 0;
			}
			
			return value;
		}
		
		private static class MutableInt
		{
			int value;
		}
	}
	
}
