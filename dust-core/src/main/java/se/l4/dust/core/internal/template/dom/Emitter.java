package se.l4.dust.core.internal.template.dom;

import java.io.IOException;

import com.google.inject.Inject;

import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.api.template.dom.Comment;
import se.l4.dust.api.template.dom.Content;
import se.l4.dust.api.template.dom.DocType;
import se.l4.dust.api.template.dom.DynamicContent;
import se.l4.dust.api.template.dom.Element;
import se.l4.dust.api.template.dom.Element.Attribute;
import se.l4.dust.api.template.dom.ParsedTemplate;
import se.l4.dust.api.template.dom.Text;
import se.l4.dust.api.template.spi.TemplateOutputStream;
import se.l4.dust.core.internal.template.TemplateContext;
import se.l4.dust.core.internal.template.components.EmittableComponent;

/**
 * Emitter of templates. Takes a {@link ParsedTemplate} and processes it 
 * sending the results to {@link TemplateOutputStream}.
 * 
 * @author Andreas Holstenson
 *
 */
public class Emitter
{
	@Inject
	public Emitter()
	{
	}
	
	public void process(ParsedTemplate template, RenderingContext ctx, Object data, TemplateOutputStream out)
		throws IOException
	{
		TemplateContext.set(ctx);
	
		DocType docType = template.getDocType();
		if(docType != null)
		{
			// Emit the document type
			out.docType(docType.getName(), docType.getPublicId(), docType.getSystemId());
		}
		
		emit(ctx, out, data, null, null, template.getRoot());
	}

	private String[] createAttributes(Element element, RenderingContext ctx, Object data)
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

	public void emit(RenderingContext ctx, 
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
				emit(ctx, out, data, lastComponent, lastData, sc);
			}
			
			out.endComment();
		}
		else if(c instanceof Element)
		{
			Element element = (Element) c;
			Content[] content = element.getRawContents();
			String[] attrs = createAttributes(element, ctx, data);
			
			out.startElement(element.getName(), attrs, false);
			
			for(Content subContent : content)
			{
				emit(ctx, out, data, lastComponent, lastData, subContent);
			}
			
			out.endElement(element.getName());
		}
	}
}
