package se.l4.dust.core.internal.template.components;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;
import java.util.RandomAccess;

import se.l4.dust.api.conversion.Conversion;
import se.l4.dust.api.conversion.NonGenericConversion;
import se.l4.dust.api.conversion.TypeConverter;
import se.l4.dust.api.template.Emittable;
import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.api.template.TemplateEmitter;
import se.l4.dust.api.template.TemplateException;
import se.l4.dust.api.template.TemplateOutputStream;
import se.l4.dust.api.template.dom.Attribute;
import se.l4.dust.api.template.fragment.FragmentEncounter;
import se.l4.dust.api.template.fragment.TemplateFragment;

import com.google.inject.Inject;


/**
 * Component that loops over a source of items.
 * 
 * @author Andreas Holstenson
 *
 */
public class LoopComponent
	implements TemplateFragment
{
	private final TypeConverter converter;

	@Inject
	public LoopComponent(TypeConverter converter)
	{
		this.converter = converter;
	}
	
	@Override
	public void build(FragmentEncounter encounter)
	{
		final Attribute<?> source = encounter.getAttribute("source", true);
		final Attribute<?> value = encounter.getAttribute("value", true);
		
		if(! value.supportsSet())
		{
			encounter.raiseError("The attribute value must be settable");
		}
		
		AbstractComponent component;
		if(source.getType().isArray())
		{
			component = new ArrayComponent(source, value, encounter.getBody());
		}
		else if(converter.canConvertBetween(source.getType(), Iterable.class))
		{
			NonGenericConversion conversion = converter.getDynamicConversion(source.getType(), Iterable.class);
			component = new IterableComponent(source, value, encounter.getBody(), conversion);
		}
		else if(converter.canConvertBetween(source.getType(), Iterator.class))
		{
			NonGenericConversion conversion = converter.getDynamicConversion(source.getType(), Iterator.class);
			component = new IteratorComponent(source, value, encounter.getBody(), conversion);
		}
		else
		{
			component = new DynamicComponent(source, value, encounter.getBody());
		}
		
		encounter.replaceWith(component);
	}
	
	private abstract class AbstractComponent
		implements Emittable
	{
		private final Attribute<?> value;
		private final Attribute<?> source;
		private final Emittable[] contents;

		public AbstractComponent(Attribute<?> source, Attribute<?> value, Emittable[] contents)
		{
			this.source = source;
			this.value = value;
			this.contents = contents;
		}
		
		protected abstract void emit(TemplateEmitter emitter, TemplateOutputStream out, Object sourceData)
			throws IOException;
		
		@Override
		public void emit(TemplateEmitter emitter, TemplateOutputStream out)
			throws IOException
		{
			Object data = emitter.getObject();
			RenderingContext ctx = emitter.getContext();
			Object sourceData = source.get(ctx, data);
			if(sourceData == null)
			{
				// TODO: Proper way to handle null?
				throw new TemplateException("Can not iterate over nothing (source is null)");
			}
			
			emit(emitter, out, sourceData);
		}
		
		protected void emitLoopContents(TemplateEmitter emitter,
				TemplateOutputStream out,
				Object o)
			throws IOException
		{
			value.set(emitter.getContext(), emitter.getObject(), o);
			
			emitter.emit(contents);
		}
	}
	
	private class DynamicComponent
		extends AbstractComponent
	{
		public DynamicComponent(Attribute<?> source, Attribute<?> value, Emittable[] contents)
		{
			super(source, value, contents);
		}
		
		@Override
		protected void emit(TemplateEmitter emitter, TemplateOutputStream out, Object sourceData)
			throws IOException
		{
			if(! (sourceData instanceof Iterable || sourceData instanceof Iterator || sourceData.getClass().isArray()))
			{
				// Try to convert to either Iterable or Iterator
				if(converter.canConvertBetween(sourceData, Iterable.class))
				{
					sourceData = converter.convert(sourceData, Iterable.class);
				}
				else if(converter.canConvertBetween(sourceData, Iterator.class))
				{
					sourceData = converter.convert(sourceData, Iterator.class);
				}
				else
				{
					throw new TemplateException("Unable to convert value of type " + sourceData.getClass() + " to either Iterable or Iterator; Value is: " + sourceData);
				}
			}
			
			if(sourceData instanceof RandomAccess && sourceData instanceof List)
			{
				List<Object> list = (List) sourceData;
				for(int i=0, n=list.size(); i<n; i++)
				{
					emitLoopContents(emitter, out, list.get(i));
				}
			}
			else if(sourceData instanceof Iterable)
			{
				Iterable<Object> items = (Iterable) sourceData;
				
				for(Object o : items)
				{
					emitLoopContents(emitter, out, o);
				}
			}
			else if(sourceData instanceof Iterator)
			{
				Iterator<Object> it = (Iterator) sourceData;
				
				while(it.hasNext())
				{
					emitLoopContents(emitter, out, it.next());
				}
			}
			else
			{
				// Array
				for(int i=0, n=Array.getLength(sourceData); i<n; i++)
				{
					Object o = Array.get(sourceData, i);
					emitLoopContents(emitter, out, o);
				}
			}
		}
	}
	
	private class IterableComponent
		extends AbstractComponent
	{
		private final Conversion<Object, Iterable<Object>> conversion;

		public IterableComponent(Attribute<?> source,
				Attribute<?> value,
				Emittable[] contents,
				Conversion<Object, Iterable<Object>> conversion)
		{
			super(source, value, contents);
			this.conversion = conversion;
		}
		
		@Override
		protected void emit(TemplateEmitter emitter, TemplateOutputStream out, Object sourceData)
			throws IOException
		{
			Iterable<Object> items = conversion.convert(sourceData);
				
			for(Object o : items)
			{
				emitLoopContents(emitter, out, o);
			}
		}
	}
	
	private class IteratorComponent
		extends AbstractComponent
	{
		private final Conversion<Object, Iterator<Object>> conversion;
	
		public IteratorComponent(Attribute<?> source,
				Attribute<?> value,
				Emittable[] contents,
				Conversion<Object, Iterator<Object>> conversion)
		{
			super(source, value, contents);
			this.conversion = conversion;
		}
		
		@Override
		protected void emit(TemplateEmitter emitter, TemplateOutputStream out, Object sourceData)
			throws IOException
		{
			Iterator<Object> it = conversion.convert(sourceData);
			while(it.hasNext())
			{
				emitLoopContents(emitter, out, it.next());
			}
		}
	}
	
	private class ArrayComponent
		extends AbstractComponent
	{
		public ArrayComponent(Attribute<?> source,
				Attribute<?> value,
				Emittable[] contents)
		{
			super(source, value, contents);
		}
		
		@Override
		protected void emit(TemplateEmitter emitter, TemplateOutputStream out, Object sourceData)
			throws IOException
		{
			for(int i=0, n=Array.getLength(sourceData); i<n; i++)
			{
				Object o = Array.get(sourceData, i);
				emitLoopContents(emitter, out, o);
			}
		}
	}
}
