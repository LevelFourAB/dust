package se.l4.dust.core.internal.template.components;

import java.io.IOException;

import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.api.template.dom.Content;
import se.l4.dust.api.template.spi.TemplateOutputStream;
import se.l4.dust.core.internal.template.dom.Emitter;

public class HolderComponent
	extends EmittableComponent
{
	public HolderComponent()
	{
		super("holder", HolderComponent.class);
	}
	
	@Override
	public Content copy()
	{
		return new HolderComponent().copyAttributes(this);
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
		for(Content c : getRawContents())
		{
			emitter.emit(ctx, out, data, this, lastData, c);
		}
	}

}
