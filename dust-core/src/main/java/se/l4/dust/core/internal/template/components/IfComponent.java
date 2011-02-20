package se.l4.dust.core.internal.template.components;

import org.jdom.Content;
import org.jdom.JDOMException;

import se.l4.dust.api.template.PropertyContent;
import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.core.internal.template.dom.ContentPreload;
import se.l4.dust.core.internal.template.dom.ExpressionParser;
import se.l4.dust.core.internal.template.dom.TemplateComponent;
import se.l4.dust.core.internal.template.dom.TemplateEmitter;
import se.l4.dust.core.template.TemplateModule;
import se.l4.dust.dom.Element;

public class IfComponent
	extends TemplateComponent
	implements ContentPreload
{
	private PropertyContent test;
	private ParameterComponent elseElement;
	
	public IfComponent()
	{
		super("if", TemplateModule.COMMON);
	}

	@Override
	public void preload(ExpressionParser expressionParser)
	{
		super.preload(expressionParser);
		
		test = getExpressionNode("test", true);
		
		elseElement = getParameter("else", false);
	}
	
	@Override
	public void process(
			TemplateEmitter emitter, 
			RenderingContext ctx,
			Element parent, 
			Object data, 
			TemplateComponent lastComponent,
			Object previousRoot)
		throws JDOMException
	{
		Object value = test.getValue(ctx, data);
		if(Boolean.TRUE.equals(value))
		{
			for(Content c : getContent())
			{
				emitter.process(ctx, data, parent, c, this, previousRoot);
			}
		}
		else if(elseElement != null)
		{
			// Render the else
			for(Content c : elseElement.getContent())
			{
				emitter.process(ctx, data, parent, c, this, previousRoot);
			}
		}
	}

}
