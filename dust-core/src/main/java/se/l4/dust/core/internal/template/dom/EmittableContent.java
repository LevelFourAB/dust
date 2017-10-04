package se.l4.dust.core.internal.template.dom;

import java.io.IOException;

import se.l4.dust.api.template.Emittable;
import se.l4.dust.api.template.TemplateEmitter;
import se.l4.dust.api.template.TemplateOutputStream;
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
	public void emit(TemplateEmitter emitter, TemplateOutputStream output)
		throws IOException
	{
		emitter.emit(emittable);
	}
}
