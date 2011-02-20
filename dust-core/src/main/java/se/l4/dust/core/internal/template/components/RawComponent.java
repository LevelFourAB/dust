package se.l4.dust.core.internal.template.components;

import org.jdom.Attribute;
import org.jdom.JDOMException;

import se.l4.dust.api.template.TemplateContext;
import se.l4.dust.core.internal.template.dom.ExpressionParser;
import se.l4.dust.core.internal.template.dom.TemplateAttribute;
import se.l4.dust.core.internal.template.dom.TemplateComponent;
import se.l4.dust.core.internal.template.dom.TemplateEmitter;
import se.l4.dust.core.template.TemplateModule;
import se.l4.dust.dom.Element;

public class RawComponent
	extends TemplateComponent
{
	private Attribute value;

	public RawComponent()
	{
		super("raw", TemplateModule.COMMON);
	}
	
	@Override
	public void preload(ExpressionParser expressionParser)
	{
		super.preload(expressionParser);
		
		value = getAttribute("value");
		if(value == null)
		{
			throwException("Raw component requires value-attribute");
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
		Object v = ((TemplateAttribute) value).getValue(ctx, root);
		
		Element e = (Element) clone();
		e.setAttribute("value", v == null ? "" : v.toString());
		parent.addContent(e);
	}

}
