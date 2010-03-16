package se.l4.dust.core.template;

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

import org.jdom.JDOMException;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import com.google.inject.Inject;

import se.l4.dust.api.annotation.Template;
import se.l4.dust.core.internal.template.dom.TemplateEmitter;
import se.l4.dust.core.internal.template.dom.TemplateOutputter;
import se.l4.dust.dom.Document;

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
	private final TemplateEmitter emitter;
	private final TemplateCache cache;
	private final Format htmlFormat;
	private final Format xmlFormat;

	@Inject
	public TemplateWriter(TemplateEmitter emitter, TemplateCache cache)
	{
		this.emitter = emitter;
		this.cache = cache;
		
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
		try
		{
			Template tpl = findAnnotation(annotations);
			
			Document template = cache.getTemplate(type, tpl);
			Document doc = emitter.process(template, t);
			
			Format format;
			if("html".equals(mediaType.getSubtype()))
			{
				format = htmlFormat;
			}
			else
			{
				format = xmlFormat;
			}
			
			XMLOutputter out = new TemplateOutputter(format);
			out.output(doc, entityStream);
		}
		catch(JDOMException e)
		{
			throw new WebApplicationException(e, 500);
		}
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
