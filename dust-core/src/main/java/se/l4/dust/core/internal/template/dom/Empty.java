package se.l4.dust.core.internal.template.dom;

import java.io.IOException;

import se.l4.dust.api.template.TemplateEmitter;
import se.l4.dust.api.template.TemplateOutputStream;
import se.l4.dust.api.template.dom.Element;

public class Empty
	extends Element
{
	public Empty()
	{
		super("");
	}
	
	@Override
	public void emit(TemplateEmitter emitter, TemplateOutputStream output)
		throws IOException
	{
		emitter.emit(contents);
	}
}
