package se.l4.dust.jaxrs.internal.template;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.jdom.output.Format;

import com.google.inject.Inject;

import se.l4.dust.api.annotation.Template;
import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.api.template.TemplateCache;
import se.l4.dust.api.template.TemplateRenderer;
import se.l4.dust.api.template.dom.ParsedTemplate;
import se.l4.dust.api.template.spi.TemplateOutputStream;
import se.l4.dust.core.template.html.HtmlTemplateOutput;

/**
 * {@link MessageBodyWriter} that renders the templates.
 * 
 * @author Andreas Holstenson
 *
 */
@Provider
@Produces({ MediaType.TEXT_HTML, MediaType.TEXT_XML })
public class TemplateWriter
	implements MessageBodyWriter<Object>
{
	private final TemplateRenderer renderer;
	private final TemplateCache cache;
	private final Format htmlFormat;
	private final Format xmlFormat;
	private final com.google.inject.Provider<RenderingContext> ctx;

	@Inject
	public TemplateWriter(TemplateRenderer renderer, TemplateCache cache,
			com.google.inject.Provider<RenderingContext> ctx)
	{
		this.renderer = renderer;
		this.cache = cache;
		this.ctx = ctx;
		
		htmlFormat = Format.getCompactFormat();
		htmlFormat.setOmitDeclaration(true);
		htmlFormat.setExpandEmptyElements(true);
		
		xmlFormat = Format.getCompactFormat();
	}
	
	public long getSize(Object t, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType)
	{
		return -1;
	}

	public boolean isWriteable(Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType)
	{
		for(Annotation a : annotations)
		{
			if(a instanceof Template)
			{
				return true;
			}
		}
		
		while(type != Object.class)
		{
			Template t = type.getAnnotation(Template.class);
			if(t != null)
			{
				return true;
			}
			
			type = type.getSuperclass();
		}
		
		return false;
	}

	public void writeTo(
			Object t, 
			Class<?> type, 
			Type genericType,
			Annotation[] annotations, 
			MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream) 
		throws IOException, WebApplicationException
	{
		Template tpl = findAnnotation(annotations);
			
		TemplateOutputStream out = new HtmlTemplateOutput(entityStream);
		ParsedTemplate template = cache.getTemplate(type, tpl);
		renderer.render(ctx.get(), template, t, out);
		out.close();
	}

	private Template findAnnotation(Annotation[] annotations)
	{
		for(Annotation a : annotations)
		{
			if(a instanceof Template)
			{
				return (Template) a;
			}
		}
		
		return null;
	}
}
