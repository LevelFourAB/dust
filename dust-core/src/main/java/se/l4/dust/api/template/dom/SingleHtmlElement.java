package se.l4.dust.api.template.dom;

import java.io.IOException;

import se.l4.dust.api.template.Emittable;
import se.l4.dust.api.template.TemplateEmitter;
import se.l4.dust.api.template.TemplateException;
import se.l4.dust.api.template.TemplateOutputStream;

public class SingleHtmlElement
	extends HtmlElement
{
	public SingleHtmlElement(String name)
	{
		super(name);
	}

	@Override
	public void emit(TemplateEmitter emitter, TemplateOutputStream output)
		throws IOException
	{
		String[] attrs = emitter.createAttributes((Attribute<String>[]) getAttributes());
		output.element(name, attrs);
	}

	@Override
	public void addContent(Emittable object)
	{
		throw new TemplateException("<" + name + "> can not have any contents");
	}
	
	@Override
	public void addContent(Iterable<? extends Emittable> objects)
	{
		throw new TemplateException("<" + name + "> can not have any contents");
	}
}
