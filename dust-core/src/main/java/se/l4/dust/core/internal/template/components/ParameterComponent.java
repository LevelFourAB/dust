package se.l4.dust.core.internal.template.components;

import org.jdom.JDOMException;

import se.l4.dust.api.template.TemplateContext;
import se.l4.dust.core.internal.template.dom.ContentPreload;
import se.l4.dust.core.internal.template.dom.ExpressionParser;
import se.l4.dust.core.internal.template.dom.TemplateComponent;
import se.l4.dust.core.internal.template.dom.TemplateEmitter;
import se.l4.dust.core.template.TemplateModule;
import se.l4.dust.dom.Element;

public class ParameterComponent
	extends TemplateComponent
	implements ContentPreload
{
	public ParameterComponent()
	{
		super("parameter", TemplateModule.COMMON);
	}
	
	@Override
	public void preload(ExpressionParser expressionParser)
	{
		super.preload(expressionParser);
		
		if(false == getParent() instanceof TemplateComponent)
		{
			throwException("parameter can only be placed as a direct descendant of a component");
		}
		
		if(getAttribute("name") == null)
		{
			throwException("parameter requires attribute name");
		}
	}
	
	@Override
	public void process(
			TemplateEmitter emitter, 
			TemplateContext ctx,
			Element parent, 
			Object root,
			TemplateComponent lastComponent, 
			Object previousRoot)
		throws JDOMException
	{
	}
}
