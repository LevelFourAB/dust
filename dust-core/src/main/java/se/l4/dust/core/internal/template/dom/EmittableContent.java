package se.l4.dust.core.internal.template.dom;

import se.l4.dust.api.template.Emittable;
import se.l4.dust.api.template.dom.Content;
import se.l4.dust.api.template.dom.Element;

public class EmittableContent
	extends Element
{
	private final Emittable emittable;

	public EmittableContent(Emittable emittable)
	{
		super("internal:emittable:" + emittable.getClass().getSimpleName());
		
		this.emittable = emittable;
	}

	public Emittable getEmittable()
	{
		return emittable;
	}
	
	@Override
	public Content doCopy()
	{
		return new EmittableContent(emittable);
	}
}
