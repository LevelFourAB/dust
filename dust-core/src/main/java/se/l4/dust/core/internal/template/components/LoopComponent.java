package se.l4.dust.core.internal.template.components;

import java.util.List;

import org.jdom.Content;
import org.jdom.JDOMException;

import com.google.inject.Singleton;

import se.l4.dust.core.internal.template.dom.ExpressionNode;
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
	public LoopComponent()
	{
		super("loop", TemplateModule.COMMON);
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
		ExpressionNode source = getExpressionNode("source", true);
		ExpressionNode value = getExpressionNode("value", true);
			
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
