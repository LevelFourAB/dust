package se.l4.dust.core.internal.template;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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

import com.google.common.collect.Maps;

/**
 * Emitter of templates. Takes a {@link ParsedTemplate} and processes it 
 * sending the results to {@link TemplateOutputStream}.
 * 
 * @author Andreas Holstenson
 *
 */
public class TemplateEmitterImpl
	implements TemplateEmitter
{
	private final ParsedTemplate template;
	private final RenderingContext ctx;

	private final String[] attrsCache;
	private final HashMap<Integer, Emittable> componentMap;
	private final HashMap<Integer, Object> dataMap;
	
	private final ElementEncounterImpl encounter;
	
	private Object current;
	private Integer currentId;
	
	private TemplateOutputStream out;
	
	private Emittable currentComponent;
	private Integer currentComponentId;
	
	private Map<String, String> extraAttributes;
	
	public TemplateEmitterImpl(ParsedTemplate template, RenderingContext ctx, Object data)
	{
		this.template = template;
		this.ctx = ctx;
		
		dataMap = new HashMap<>(32);
		componentMap = new HashMap<>(32);
		
		current = data;
		currentId = template.getRawId();
		dataMap.put(currentId, current);
		
		componentMap.put(currentId, template.getRoot());
		attrsCache = new String[20*2];
		
		encounter = new ElementEncounterImpl();
		extraAttributes = Maps.newHashMap();
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
	public String[] createAttributes(Attribute<String>[] attributes)
	{
		return createAttributes(attributes, current);
	}
	
	private String[] createAttributes(Attribute<String>[] attributes, Object data)
	{
		String[] attrs;
		if(attributes.length >= 10)
		{
			attrs = new String[attributes.length * 2];
		}
		else
		{
			attrs = attrsCache;
			attrs[attributes.length * 2] = null;
		}
		
		for(int i=0, n=attributes.length*2; i<n; i+=2)
		{
			Attribute<String> attr = attributes[i/2];
			attrs[i] = attr.getName();
			String value = attr.get(ctx, data);
			
			String extra = extraAttributes.remove(attr.getName());
			if(extra != null)
			{
				// TODO: Different modes for attributes?
				if(value == null || value.isEmpty())
				{
					value = extra;
				}
				else
				{
					value += ' ' + extra;
				}
			}
			
			attrs[i+1] = value;
		}
		
		if(! extraAttributes.isEmpty())
		{
			int i = attributes.length;
			if(i + extraAttributes.size()*2 > attrs.length)
			{
				attrs = Arrays.copyOf(attrs, i + extraAttributes.size()*2);
			}
			
			for(Map.Entry<String, String> e : extraAttributes.entrySet())
			{
				attrs[i] = e.getKey();
				attrs[i+1] = e.getValue();
				i += 2;
			}
		}
		extraAttributes.clear();
		
		return attrs;
	}
	
	@Override
	public Emittable getParameter(String name)
	{
		if(currentComponent instanceof Element)
		{
			return ((Element) currentComponent).getParameter(name);
		}
		
		return null;
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
	
	public Emittable getCurrentComponent()
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
		encounter.wrapped = we.getElement();
		encounter.skip = false;
		wrapper.beforeElement(encounter);
		
		if(! encounter.skip)
		{
			emit(encounter.wrapped);
		}
		
		wrapper.afterElement(encounter);
		encounter.reset();
	}
	
	private class ElementEncounterImpl
		implements ElementEncounter
	{
		private boolean skip;
		private Element wrapped;
		
		@Override
		public RenderingContext getContext()
		{
			return TemplateEmitterImpl.this.getContext();
		}
		
		@Override
		public Object getObject()
		{
			return TemplateEmitterImpl.this.getObject();
		}
		
		@Override
		public void skip()
		{
			skip = true;
		}
		
		@Override
		public void emit()
			throws IOException
		{
			skip = true;
			TemplateEmitterImpl.this.emit(wrapped);
		}
		
		@Override
		public TemplateOutputStream getOutput()
		{
			return out;
		}
		
		@Override
		public void pushAttribute(String name, String value)
		{
			extraAttributes.put(name, value);
		}
		
		public void reset()
		{
			extraAttributes.clear();
		}
	}
}
