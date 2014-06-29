package se.l4.dust.api.template.dom;

import java.io.IOException;

import se.l4.dust.api.template.Emittable;
import se.l4.dust.api.template.TemplateEmitter;
import se.l4.dust.api.template.TemplateOutputStream;

public class HtmlElement
	extends Element
{
	public HtmlElement(String name)
	{
		super(name);
	}

	@Override
	public void emit(TemplateEmitter emitter, TemplateOutputStream output)
		throws IOException
	{
		Emittable[] content = getRawContents();
		String[] attrs = emitter.createAttributes((Attribute<String>[]) getAttributes());
		
		output.startElement(name, attrs);
		
		emitter.emit(content);
		
		output.endElement(name);
	}

}
