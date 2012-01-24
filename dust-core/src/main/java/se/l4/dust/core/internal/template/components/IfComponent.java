package se.l4.dust.core.internal.template.components;

import java.io.IOException;

import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.api.template.dom.Content;
import se.l4.dust.api.template.spi.TemplateOutputStream;
import se.l4.dust.core.internal.template.dom.Emitter;

public class IfComponent
	extends EmittableComponent
{
	public IfComponent()
	{
		super("if", IfComponent.class);
	}
	
	@Override
	public Content copy()
	{
		return new IfComponent().copyAttributes(this);
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
		Attribute test = getAttribute("test");
		Object value = test == null ? true : test.getValue(ctx, data);
		ParameterComponent elseElement = getParameter("else", false);
		
		if(Boolean.TRUE.equals(value))
		{
			for(Content c : getRawContents())
			{
				if(c == elseElement) continue;
			
				emitter.emit(out, data, lastComponent, lastData, c);
			}
		}
		else if(elseElement != null)
		{
			// Render the else
			for(Content c : elseElement.getRawContents())
			{
				emitter.emit(out, data, lastComponent, lastData, c);
			}
		}
	}

}
