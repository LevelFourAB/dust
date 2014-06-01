package se.l4.dust.api.template.dom;

import java.io.IOException;

import se.l4.dust.api.template.TemplateEmitter;
import se.l4.dust.api.template.TemplateOutputStream;
import se.l4.dust.api.template.mixin.ElementWrapper;
import se.l4.dust.core.internal.template.dom.TemplateEmitterImpl;

/**
 * Holder for an element that has been wrapped by a mixin.
 * 
 * @author Andreas Holstenson
 *
 */
public class WrappedElement
	implements Content
{
	private final Element element;
	private final ElementWrapper wrapper;

	public WrappedElement(Element element, ElementWrapper wrapper)
	{
		this.element = element;
		this.wrapper = wrapper;
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
	public String getDebugSource()
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
	public void withDebugInfo(String source, int line, int column)
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
