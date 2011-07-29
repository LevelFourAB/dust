package se.l4.dust.core.internal.template.components;

import java.io.IOException;
import java.util.Collection;

import se.l4.dust.api.TemplateException;
import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.api.template.dom.Content;
import se.l4.dust.api.template.spi.TemplateOutputStream;
import se.l4.dust.core.internal.template.dom.Emitter;


/**
 * Component that loops over a source of items.
 * 
 * @author Andreas Holstenson
 *
 */
public class LoopComponent
	extends EmittableComponent
{
	public LoopComponent()
	{
		super("loop", LoopComponent.class);
	}
	
	@Override
	public Content copy()
	{
		return new LoopComponent().copyAttributes(this);
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
			TemplateOutputStream out,
			Object data,
			EmittableComponent lastComponent,
			Object lastData)
		throws IOException
	{
		Attribute source = getAttribute("source");
		if(source == null) throw new TemplateException("Attribute source is required");
		
		Attribute value = getAttribute("value");
		if(value == null) throw new TemplateException("Attribute value is required");
		
		Object sourceData = source.getValue(ctx, data);
		// TODO: Use conversions
		Collection<Object> items = (Collection) sourceData;
		
		for(Object o : items)
		{
			value.setValue(ctx, data, o);
			
			for(Content c : getRawContents())
			{
				emitter.emit(ctx, out, data, this, lastData, c);
			}
		}
	}
	
	
}
