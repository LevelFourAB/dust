package se.l4.dust.core.internal.template.components;

import java.util.List;

import org.jdom.Content;
import org.jdom.JDOMException;

import com.google.inject.Singleton;

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
@Singleton
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
		List<Object> items = (List) sourceData;
		
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
