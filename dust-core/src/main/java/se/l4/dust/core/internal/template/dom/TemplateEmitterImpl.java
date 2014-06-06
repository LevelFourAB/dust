package se.l4.dust.core.internal.template.dom;

import java.io.IOException;
import java.util.HashMap;

import se.l4.dust.api.template.Emittable;
import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.api.template.TemplateEmitter;
import se.l4.dust.api.template.TemplateException;
import se.l4.dust.api.template.TemplateOutputStream;
import se.l4.dust.api.template.dom.Attribute;
import se.l4.dust.api.template.dom.DocType;
import se.l4.dust.api.template.dom.Element;
import se.l4.dust.api.template.dom.ParsedTemplate;
import se.l4.dust.api.template.dom.WrappedElement;
import se.l4.dust.api.template.mixin.ElementEncounter;
import se.l4.dust.api.template.mixin.ElementWrapper;

/**
 * Emitter of templates. Takes a {@link ParsedTemplate} and processes it 
 * sending the results to {@link TemplateOutputStream}.
 * 
 * @author Andreas Holstenson
 *
 */
public class TemplateEmitterImpl
	implements ElementEncounter, TemplateEmitter
{
	private final ParsedTemplate template;
	private final RenderingContext ctx;

	private final String[] attrsCache;
	private final HashMap<Integer, Element> componentMap;
	private final HashMap<Integer, Object> dataMap;
	
	private Object current;
	private Integer currentId;
	
	private TemplateOutputStream out;
	
	private Element currentComponent;
	private Integer currentComponentId;
	
	private boolean skip;
	private Element wrapped;
	
	public TemplateEmitterImpl(ParsedTemplate template, RenderingContext ctx, Object data)
	{
		this.template = template;
		this.ctx = ctx;
		
		dataMap = new HashMap<Integer, Object>(32);
		componentMap = new HashMap<Integer, Element>(32);
		
		current = data;
		currentId = template.getRawId();
		dataMap.put(currentId, current);
		
		componentMap.put(currentId, template.getRoot());
		attrsCache = new String[10*2];
	}
	
	public void process(TemplateOutputStream out)
		throws IOException
	{
		this.out = out;
		
		DocType docType = template.getDocType();
		if(docType != null)
		{
			// Emit the document type
			out.docType(docType.getName(), docType.getPublicId(), docType.getSystemId());
		}
		
		emit(template.getRoot());
	}

	@Override
	public String[] createAttributes(Element element)
	{
		return createAttributes(element, current);
	}
	
	private String[] createAttributes(Element element, Object data)
	{
		Attribute[] rawAttrs = element.getAttributes();
		String[] attrs;
		if(rawAttrs.length >= 10)
		{
			attrs = new String[rawAttrs.length * 2];
		}
		else
		{
			attrs = attrsCache;
			attrs[rawAttrs.length * 2] = null;
		}
		
		for(int i=0, n=rawAttrs.length*2; i<n; i+=2)
		{
			Attribute attr = rawAttrs[i/2];
			attrs[i] = attr.getName();
			attrs[i+1] = attr.getStringValue(ctx, data);
		}
		return attrs;
	}

	@Override
	public void emit(Emittable c)
		throws IOException
	{
		try
		{
			c.emit(this, out);
		}
		catch(Exception e)
		{
			if(e instanceof TemplateException)
			{
				throw ((TemplateException) e).withDebugInfo(c);
			}
			
			throw new TemplateException(e.getMessage(), e).withDebugInfo(c);
		}
	}
	
	@Override
	public void emit(Emittable[] emittables)
		throws IOException
	{
		int i = 0;
		try
		{
			int n = emittables.length;
			for(i=0; i<n; i++)
			{
				emittables[i].emit(this, out);
			}
		}
		catch(Exception e)
		{
			if(e instanceof TemplateException)
			{
				throw ((TemplateException) e).withDebugInfo(emittables[i]);
			}
			
			throw new TemplateException(e.getMessage(), e).withDebugInfo(emittables[i]);
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
	
	@Override
	public void skip()
	{
		this.skip = true;
	}
	
	@Override
	public void emit()
		throws IOException
	{
		this.skip = true;
		emit(wrapped);
	}
	
	@Override
	public TemplateOutputStream getOutput()
	{
		return out;
	}
	
	public Element getCurrentComponent()
	{
		return currentComponent;
	}
	
	public Integer getCurrentDataId()
	{
		return currentId;
	}
	
	public Object getCurrentData()
	{
		return current;
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

	public void emitWrapped(WrappedElement we)
		throws IOException
	{
		ElementWrapper wrapper = we.getWrapper();
		this.wrapped = we.getElement();
		skip = false;
		wrapper.beforeElement(this);
		
		if(! skip)
		{
			emit(this.wrapped);
		}
		
		wrapper.afterElement(this);
	}
}
