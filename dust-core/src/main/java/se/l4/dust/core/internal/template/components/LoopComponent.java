package se.l4.dust.core.internal.template.components;

import java.util.Collection;

import org.jdom.Content;
import org.jdom.JDOMException;

import se.l4.dust.api.template.PropertyContent;
import se.l4.dust.core.internal.template.dom.ExpressionParser;
import se.l4.dust.core.internal.template.dom.TemplateComponent;
import se.l4.dust.core.internal.template.dom.TemplateEmitter;
import se.l4.dust.core.template.TemplateModule;
import se.l4.dust.dom.Element;


/**
 * Component that loops over a source of items.
 * 
 * @author Andreas Holstenson
 *
 */
public class LoopComponent
	extends TemplateComponent
{
	private PropertyContent source;
	private PropertyContent value;

	public LoopComponent()
	{
		super("loop", TemplateModule.COMMON);
	}
	
	@Override
	public void preload(ExpressionParser expressionParser)
	{
		super.preload(expressionParser);
		
		source = getExpressionNode("source", true);
		value = getExpressionNode("value", true);
	}
	
	@Override
	public void process(
			TemplateEmitter emitter, 
			Element parent, 
			Object data,
			TemplateComponent lastComponent,
			Object previousRoot)
		throws JDOMException
	{
		Object sourceData = source.getValue(data);
		// TODO: Use conversions
		Collection<Object> items = (Collection) sourceData;
		
		for(Object o : items)
		{
			value.setValue(data, o);
			
			for(Content c : getContent())
			{
				emitter.process(data, parent, c, this, previousRoot);
			}
		}
	}
	
	
}
