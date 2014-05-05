package se.l4.dust.core.internal.template.components;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;
import java.util.RandomAccess;

import se.l4.dust.api.TemplateException;
import se.l4.dust.api.conversion.TypeConverter;
import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.api.template.dom.Content;
import se.l4.dust.api.template.spi.TemplateOutputStream;
import se.l4.dust.core.internal.template.dom.Emitter;

import com.google.inject.Inject;


/**
 * Component that loops over a source of items.
 * 
 * @author Andreas Holstenson
 *
 */
public class LoopComponent
	extends EmittableComponent
{
	private final TypeConverter converter;

	@Inject
	public LoopComponent(TypeConverter converter)
	{
		super("loop", LoopComponent.class);
		
		this.converter = converter;
	}
	
	@Override
	public Content doCopy()
	{
		return new LoopComponent(converter).copyAttributes(this);
	}
	
	public Attribute getSource()
	{
		Attribute source = getAttribute("source");
		if(source == null) throw new TemplateException("Attribute source is required");
		return source;
	}
	
	public Attribute getValue()
	{
		Attribute value = getAttribute("value");
		if(value == null) throw new TemplateException("Attribute value is required");
		return value;
	}
	
	@Override
	public void emit(
			Emitter emitter,
			RenderingContext ctx, 
			TemplateOutputStream out)
		throws IOException
	{
		Attribute source = getAttribute("source");
		if(source == null) throw new TemplateException("Attribute source is required");
		
		Attribute value = getAttribute("value");
		if(value == null) throw new TemplateException("Attribute value is required");
		
		Object data = emitter.getObject();
		Object sourceData = source.getValue(ctx, data);
		if(sourceData == null)
		{
			// TODO: Proper way to handle null?
			throw new TemplateException("Can not iterate over nothing (source is null)");
		}
		
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
				emitLoopContents(emitter, ctx, out, data, value, list.get(i));
			}
		}
		else if(sourceData instanceof Iterable)
		{
			Iterable<Object> items = (Iterable) sourceData;
			
			for(Object o : items)
			{
				emitLoopContents(emitter, ctx, out, data, value, o);
			}
		}
		else if(sourceData instanceof Iterator)
		{
			Iterator<Object> it = (Iterator) sourceData;
			
			while(it.hasNext())
			{
				Object o = it.next();
				
				emitLoopContents(emitter, ctx, out, data, value, o);
			}
		}
		else
		{
			// Array
			for(int i=0, n=Array.getLength(sourceData); i<n; i++)
			{
				Object o = Array.get(sourceData, i);
				emitLoopContents(emitter, ctx, out, data, value, o);
			}
		}
	}

	private void emitLoopContents(Emitter emitter, RenderingContext ctx,
			TemplateOutputStream out, Object data, 
			Attribute value, Object o)
		throws IOException
	{
		value.setValue(ctx, data, o);
		
		for(Content c : getRawContents())
		{
			emitter.emit(out, c);
		}
	}
	
	
}
