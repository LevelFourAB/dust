package se.l4.dust.core.internal.template.components;

import org.jdom.Content;
import org.jdom.JDOMException;

import com.google.inject.Singleton;

import se.l4.dust.core.internal.template.dom.TemplateComponent;
import se.l4.dust.core.internal.template.dom.TemplateEmitter;
import se.l4.dust.core.template.TemplateModule;
import se.l4.dust.dom.Element;

@Singleton
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
			Element parent, 
			Object root,
			TemplateComponent lastComponent, 
			Object previousRoot)
		throws JDOMException
	{
		for(Content c : getContent())
		{
			emitter.process(root, parent, c, lastComponent, previousRoot);
		}
	}
}
