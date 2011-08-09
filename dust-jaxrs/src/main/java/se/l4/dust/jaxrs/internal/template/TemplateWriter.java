package se.l4.dust.jaxrs.internal.template;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import se.l4.dust.api.annotation.Template;
import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.api.template.TemplateCache;
import se.l4.dust.api.template.TemplateRenderer;
import se.l4.dust.api.template.dom.ParsedTemplate;
import se.l4.dust.api.template.spi.TemplateOutputStream;
import se.l4.dust.core.template.html.HtmlTemplateOutput;
import se.l4.dust.jaxrs.spi.WebRenderingContext;

import com.google.inject.Inject;

/**
 * {@link MessageBodyWriter} that renders the templates.
 * 
 * @author Andreas Holstenson
 *
 */
@Provider
@Produces({ "text/html; charset=utf-8", MediaType.TEXT_XML })
public class TemplateWriter
	implements MessageBodyWriter<Object>
{
	private final TemplateRenderer renderer;
	private final TemplateCache cache;
	private final com.google.inject.Provider<RenderingContext> ctx;
	private final com.google.inject.Provider<HttpServletRequest> requests;

	@Inject
	public TemplateWriter(TemplateRenderer renderer, TemplateCache cache,
			com.google.inject.Provider<RenderingContext> ctx,
			com.google.inject.Provider<HttpServletRequest> requests)
	{
		this.renderer = renderer;
		this.cache = cache;
		this.ctx = ctx;
		this.requests = requests;
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
		
		RenderingContext context = ctx.get();
		if(context instanceof WebRenderingContext)
		{
			((WebRenderingContext) context).setup(requests.get());
		}
		
		ParsedTemplate template = cache.getTemplate(context, type, tpl);

		// FIXME: Should we really do this?
		httpHeaders.putSingle("Content-Type", "text/html; charset=utf-8");
		
		TemplateOutputStream out = new HtmlTemplateOutput(entityStream);
		renderer.render(context, template, t, out);
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
