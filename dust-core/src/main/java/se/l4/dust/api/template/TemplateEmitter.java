package se.l4.dust.api.template;

import java.io.IOException;

import se.l4.dust.api.template.dom.Content;
import se.l4.dust.api.template.spi.TemplateOutputStream;

public interface TemplateEmitter
{
	Object getObject();

	RenderingContext getContext();

	void emit(TemplateOutputStream output, Content c)
		throws IOException;
}
