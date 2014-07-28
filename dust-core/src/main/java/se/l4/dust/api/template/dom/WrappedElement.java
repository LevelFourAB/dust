package se.l4.dust.api.template.dom;

import java.io.IOException;

import se.l4.dust.api.resource.ResourceLocation;
import se.l4.dust.api.template.Emittable;
import se.l4.dust.api.template.TemplateEmitter;
import se.l4.dust.api.template.TemplateOutputStream;
import se.l4.dust.api.template.mixin.ElementWrapper;
import se.l4.dust.core.internal.template.TemplateEmitterImpl;

/**
 * Holder for an element that has been wrapped by a mixin.
 * 
 * @author Andreas Holstenson
 *
 */
public class WrappedElement
	extends Element
{
	private final Element element;
	private final ElementWrapper wrapper;

	public WrappedElement(Element element, ElementWrapper wrapper)
	{
		super("internal-wrapper");
		
		this.element = element;
		this.wrapper = wrapper;
	}
	
	@Override
	public void setContents(Content[] newContent)
	{
		element.setContents(newContent);
	}
	
	@Override
	public void addAttribute(Attribute<?> attribute)
	{
		element.addAttribute(attribute);
	}
	
	@Override
	public void addContent(Emittable object)
	{
		element.addContent(object);
	}
	
	@Override
	public void addContent(Iterable<? extends Emittable> objects)
	{
		element.addContent(objects);
	}
	
	@Override
	public void addParameter(String name, Emittable content)
	{
		element.addParameter(name, content);
	}
	
	@Override
	public Emittable getParameter(String name)
	{
		return element.getParameter(name);
	}

	public Element getElement()
	{
		return element;
	}
	
	public ElementWrapper getWrapper()
	{
		return wrapper;
	}
	
	@Override
	public ResourceLocation getDebugSource()
	{
		return element.getDebugSource();
	}
	
	@Override
	public int getLine()
	{
		return element.getLine();
	}
	
	@Override
	public int getColumn()
	{
		return element.getColumn();
	}
	
	@Override
	public void withDebugInfo(ResourceLocation source, int line, int column)
	{
	}
	
	@Override
	public void emit(TemplateEmitter emitter, TemplateOutputStream output)
		throws IOException
	{
		TemplateEmitterImpl emitterImpl = (TemplateEmitterImpl) emitter;
		emitterImpl.emitWrapped(this);
	}
}
