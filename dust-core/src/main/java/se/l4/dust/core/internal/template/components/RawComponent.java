package se.l4.dust.core.internal.template.components;

import org.jdom.Content;
import org.jdom.JDOMException;

import se.l4.dust.core.internal.template.dom.TemplateComponent;
import se.l4.dust.core.internal.template.dom.TemplateEmitter;
import se.l4.dust.core.template.TemplateModule;
import se.l4.dust.dom.Element;

public class RawComponent
	extends TemplateComponent
{
	public RawComponent()
	{
		super("raw", TemplateModule.COMMON);
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
		parent.addContent((Content) clone());
	}

}
