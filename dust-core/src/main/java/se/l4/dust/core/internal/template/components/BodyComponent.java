package se.l4.dust.core.internal.template.components;

import org.jdom.Content;
import org.jdom.JDOMException;

import com.google.inject.Singleton;

import se.l4.dust.core.internal.template.dom.TemplateComponent;
import se.l4.dust.core.internal.template.dom.TemplateEmitter;
import se.l4.dust.core.template.TemplateModule;
import se.l4.dust.dom.Element;

@Singleton
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
				emitter.process(previousRoot, parent, c, this, previousRoot);
			}
		}
	}

}
