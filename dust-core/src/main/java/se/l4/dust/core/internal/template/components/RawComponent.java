package se.l4.dust.core.internal.template.components;

import java.io.IOException;

import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.api.template.spi.TemplateOutputStream;
import se.l4.dust.core.internal.template.dom.Emitter;

public class RawComponent
	extends EmittableComponent
{
	public RawComponent()
	{
		super("raw", RawComponent.class);
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
		Attribute attribute = getAttribute("value");
		String output = attribute.getStringValue(ctx, data);

		out.text(output);
	}
}
