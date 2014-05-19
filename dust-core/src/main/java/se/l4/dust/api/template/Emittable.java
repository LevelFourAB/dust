package se.l4.dust.api.template;

import java.io.IOException;

import se.l4.dust.api.template.spi.TemplateOutputStream;

public interface Emittable
{
	void emit(TemplateEmitter emitter, TemplateOutputStream output)
		throws IOException;
}
