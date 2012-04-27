package se.l4.dust.core.internal.template.dom;

import java.io.IOException;
import java.util.HashMap;

import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.api.template.dom.Comment;
import se.l4.dust.api.template.dom.Content;
import se.l4.dust.api.template.dom.DocType;
import se.l4.dust.api.template.dom.DynamicContent;
import se.l4.dust.api.template.dom.Element;
import se.l4.dust.api.template.dom.Element.Attribute;
import se.l4.dust.api.template.dom.ParsedTemplate;
import se.l4.dust.api.template.dom.Text;
import se.l4.dust.api.template.dom.WrappedElement;
import se.l4.dust.api.template.mixin.ElementEncounter;
import se.l4.dust.api.template.mixin.ElementWrapper;
import se.l4.dust.api.template.spi.TemplateOutputStream;
import se.l4.dust.core.internal.template.components.EmittableComponent;

/**
 * Emitter of templates. Takes a {@link ParsedTemplate} and processes it 
 * sending the results to {@link TemplateOutputStream}.
 * 
 * @author Andreas Holstenson
 *
 */
public class Emitter
	implements ElementEncounter
{
	private final ParsedTemplate template;
	private final RenderingContext ctx;

	private Object current;
	private Integer currentId;
	private final HashMap<Integer, Object> dataMap;
	
	private Element currentComponent;
	private Integer currentComponentId;
	private final HashMap<Integer, Element> componentMap;
	
	public Emitter(ParsedTemplate template, RenderingContext ctx, Object data)
	{
		this.template = template;
		this.ctx = ctx;
		
		dataMap = new HashMap<Integer, Object>(32);
		componentMap = new HashMap<Integer, Element>(32);
		
		current = data;
		currentId = template.getRawId();
		dataMap.put(currentId, current);
		
		componentMap.put(currentId, template.getRoot());
	}
	
	public void process(TemplateOutputStream out)
		throws IOException
	{
		DocType docType = template.getDocType();
		if(docType != null)
		{
			// Emit the document type
			out.docType(docType.getName(), docType.getPublicId(), docType.getSystemId());
		}
		
		emit(out, template.getRoot());
	}

	private String[] createAttributes(Element element, Object data)
	{
		Attribute[] rawAttrs = element.getAttributes();
		String[] attrs = new String[rawAttrs.length * 2];
		for(int i=0, n=attrs.length; i<n; i+=2)
		{
			Attribute attr = rawAttrs[i/2];
			attrs[i] = attr.getName();
			attrs[i+1] = attr.getStringValue(ctx, data);
		}
		return attrs;
	}

	public void emit(TemplateOutputStream out, Content c)
		throws IOException
	{
		if(c instanceof EmittableComponent)
		{
			((EmittableComponent) c).emit(this, ctx, out);
		}
		else if(c instanceof DynamicContent)
		{
			Object value = ctx.getDynamicValue((DynamicContent) c, current);
			out.text(ctx.getStringValue(value));
		}
		else if(c instanceof Text)
		{
			out.text(((Text) c).getText());
		}
		else if(c instanceof Comment)
		{
			out.startComment();
			
			for(Content sc : ((Comment) c).getRawContents())
			{
				emit(out, sc);
			}
			
			out.endComment();
		}
		else if(c instanceof Element)
		{
			Element element = (Element) c;
			Content[] content = element.getRawContents();
			String[] attrs = createAttributes(element, current);
			
			out.startElement(element.getName(), attrs, false);
			
			for(Content subContent : content)
			{
				emit(out, subContent);
			}
			
			out.endElement(element.getName());
		}
		else if(c instanceof WrappedElement)
		{
			WrappedElement we = (WrappedElement) c;
			ElementWrapper wrapper = we.getWrapper();
			wrapper.beforeElement(this);
			
			emit(out, we.getElement());
			
			wrapper.afterElement(this);
		}
		else
		{
			throw new AssertionError("Found content that can not be emitted: " + c);
		}
	}
	
	@Override
	public RenderingContext getContext()
	{
		return ctx;
	}
	
	@Override
	public Object getObject()
	{
		return current;
	}
	
	public Element getCurrentComponent()
	{
		return currentComponent;
	}
	
	public Integer switchData(Integer id)
	{
		Integer old = currentId;
		current = dataMap.get(id);
		currentId = id;
		
		return old;
	}

	public Integer switchData(Integer id, Object data)
	{
		Integer old = currentId;
		dataMap.put(id, data);
		current = data;
		currentId = id;
		
		return old;
	}

	public Integer switchComponent(Integer id)
	{
		Integer old = currentComponentId;
		currentComponent = componentMap.get(id);
		currentComponentId = id;;
		
		return old;
	}

	public Integer switchComponent(Integer id, Element data)
	{
		Integer old = currentComponentId;
		componentMap.put(id, data);
		currentComponent = data;
		currentComponentId = id;
		
		return old;
	}
}
