package se.l4.dust.core.internal.template.dom;

import java.io.IOException;

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
	private final Object data;

	private Object current;
	
	public Emitter(ParsedTemplate template, RenderingContext ctx, Object data)
	{
		this.template = template;
		this.ctx = ctx;
		this.data = data;
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
		
		emit(out, data, null, null, template.getRoot());
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

	public void emit(
			TemplateOutputStream out, 
			Object data, 
			EmittableComponent lastComponent, 
			Object lastData, 
			Content c)
		throws IOException
	{
		if(c instanceof EmittableComponent)
		{
			((EmittableComponent) c).emit(this, ctx, out, data, lastComponent, lastData);
		}
		else if(c instanceof DynamicContent)
		{
			Object value = ctx.getDynamicValue((DynamicContent) c, data);
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
				emit(out, data, lastComponent, lastData, sc);
			}
			
			out.endComment();
		}
		else if(c instanceof Element)
		{
			Element element = (Element) c;
			Content[] content = element.getRawContents();
			String[] attrs = createAttributes(element, data);
			
			out.startElement(element.getName(), attrs, false);
			
			for(Content subContent : content)
			{
				emit(out, data, lastComponent, lastData, subContent);
			}
			
			out.endElement(element.getName());
		}
		else if(c instanceof WrappedElement)
		{
			this.current = data;
			
			WrappedElement we = (WrappedElement) c;
			ElementWrapper wrapper = we.getWrapper();
			wrapper.beforeElement(this);
			
			emit(out, data, lastComponent, lastData, we.getElement());
			
			this.current = data;
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
}
