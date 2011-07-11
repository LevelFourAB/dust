package se.l4.dust.core.internal.template.components;

import java.io.IOException;

import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.api.template.spi.TemplateOutputStream;
import se.l4.dust.core.internal.template.dom.Emitter;

public class ParameterComponent
	extends EmittableComponent
{
	public ParameterComponent()
	{
		super("parameter", ParameterComponent.class);
	}
	
//	@Override
//	public void preload(ExpressionParser expressionParser)
//	{
//		super.preload(expressionParser);
//		
//		if(false == getParent() instanceof TemplateComponent)
//		{
//			throwException("parameter can only be placed as a direct descendant of a component");
//		}
//		
//		if(getAttribute("name") == null)
//		{
//			throwException("parameter requires attribute name");
//		}
//	}
	
//	@Override
//	public void process(
//			TemplateEmitter emitter, 
//			RenderingContext ctx,
//			Element parent, 
//			Object root,
//			TemplateComponent lastComponent, 
//			Object previousRoot)
//		throws JDOMException
//	{
//	}
	
	@Override
	public void emit(
			Emitter emitter,
			RenderingContext ctx, 
			TemplateOutputStream out,
			Object data,
			EmittableComponent lastComponent,
			Object lastData)
		throws IOException
	{
	}
}
