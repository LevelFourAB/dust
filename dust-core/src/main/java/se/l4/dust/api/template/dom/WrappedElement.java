package se.l4.dust.api.template.dom;

import se.l4.dust.api.template.mixin.ElementWrapper;

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
	private Element parent;

	public WrappedElement(Element element, ElementWrapper wrapper)
	{
		this.element = element;
		this.wrapper = wrapper;
	}

	@Override
	public Element getParent()
	{
		return parent;
	}

	@Override
	public void setParent(Element element)
	{
		this.parent = element;
	}

	@Override
	public Content copy()
	{
		return new WrappedElement(element, wrapper);
	}

	public Element getElement()
	{
		return element;
	}
	
	public ElementWrapper getWrapper()
	{
		return wrapper;
	}
}
