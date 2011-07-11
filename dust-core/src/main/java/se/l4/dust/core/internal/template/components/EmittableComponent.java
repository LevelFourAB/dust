package se.l4.dust.core.internal.template.components;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import se.l4.dust.api.TemplateException;
import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.api.template.dom.Component;
import se.l4.dust.api.template.dom.Content;
import se.l4.dust.api.template.dom.Element;
import se.l4.dust.api.template.spi.TemplateOutputStream;
import se.l4.dust.core.internal.template.dom.Emitter;

public abstract class EmittableComponent
	extends Component
{
	public EmittableComponent(String name, Class<?> component, String... attributes)
	{
		super(name, component, attributes);
	}

	public abstract void emit(
			Emitter emitter,
			RenderingContext ctx, 
			TemplateOutputStream out,
			Object data,
			EmittableComponent lastComponent,
			Object lastData)
		throws IOException;
	
	protected ParameterComponent getParameter(String name, boolean required)
	{
		for(Content c : getRawContents())
		{
			if(c instanceof ParameterComponent)
			{
				ParameterComponent pc = (ParameterComponent) c;
				Attribute attr = pc.getAttribute("name");
				if(attr != null && attr.getStringValue().equals(name))
				{
					return pc;
				}
			}
		}
		
		if(required)
		{
			List<String> path = new ArrayList<String>();
			Element e = this;
			while(e != null)
			{
				path.add(e.getName());
				e = e.getParent();
			}
			Collections.reverse(path);
			
			StringBuilder builder = new StringBuilder();
			for(int i=0, n=path.size(); i<n; i++)
			{
				if(i > 0) builder.append(" > ");
				
				builder.append(path.get(i));
			}
			
			throw new TemplateException("Parameter " + name + " is required, path to component is " + builder);
		}
		
		return null;
	}
}
