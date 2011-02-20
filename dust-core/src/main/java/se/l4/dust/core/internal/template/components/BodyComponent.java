package se.l4.dust.core.internal.template.components;

import org.jdom.Content;
import org.jdom.JDOMException;

import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.core.internal.template.dom.TemplateComponent;
import se.l4.dust.core.internal.template.dom.TemplateEmitter;
import se.l4.dust.core.template.TemplateModule;
import se.l4.dust.dom.Element;

public class BodyComponent
	extends TemplateComponent
{
	public BodyComponent()
	{
		super("body", TemplateModule.COMMON);
	}
	
	@Override
	public void process(
			TemplateEmitter emitter, 
			RenderingContext ctx,
			Element parent, 
			Object root,
			TemplateComponent lastComponent,
			Object previousRoot)
		throws JDOMException
	{
		if(lastComponent != null)
		{
			for(Content c : lastComponent.getContent())
			{
				emitter.process(ctx, previousRoot, parent, c, this, previousRoot);
			}
		}
	}

}
