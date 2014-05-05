package se.l4.dust.core.internal.template.components;

import java.io.IOException;

import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.api.template.dom.Content;
import se.l4.dust.api.template.dom.Element;
import se.l4.dust.api.template.spi.TemplateOutputStream;
import se.l4.dust.core.internal.template.dom.Emitter;

public class BodyComponent
	extends EmittableComponent
{
	public BodyComponent()
	{
		super("body", BodyComponent.class);
	}
	
	@Override
	public Content doCopy()
	{
		return new BodyComponent().copyAttributes(this);
	}
	
	@Override
	public void emit(
			Emitter emitter,
			RenderingContext ctx, 
			TemplateOutputStream out)
		throws IOException
	{
		Element lastComponent = emitter.getCurrentComponent();
		
		if(lastComponent != null)
		{
			Attribute attr = getAttribute("parameter");
			if(attr != null)
			{
				Object data = emitter.getObject();
				
				String paramName = attr.getStringValue(ctx, data);
				ParameterComponent param = getParameter(lastComponent, paramName, false);
				if(param != null)
				{
					for(Content c : param.getRawContents())
					{
						emitter.emit(out, c);
					}
				}
			}
			else
			{
				emitter.emit(out, lastComponent);
			}
		}
	}

}
