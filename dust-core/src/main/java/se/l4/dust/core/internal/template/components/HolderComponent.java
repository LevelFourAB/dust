package se.l4.dust.core.internal.template.components;

import org.jdom.Content;
import org.jdom.JDOMException;

import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.core.internal.template.TemplateModule;
import se.l4.dust.core.internal.template.dom.TemplateComponent;
import se.l4.dust.core.internal.template.dom.TemplateEmitter;
import se.l4.dust.dom.Element;

public class HolderComponent
	extends TemplateComponent
{
	public HolderComponent()
	{
		super("holder", TemplateModule.COMMON);
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
		for(Content c : getContent())
		{
			emitter.process(ctx, root, parent, c, lastComponent, previousRoot);
		}
	}
}
