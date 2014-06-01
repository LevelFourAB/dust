package se.l4.dust.api.template;

import java.io.IOException;

public interface Emittable
{
	void emit(TemplateEmitter emitter, TemplateOutputStream output)
		throws IOException;
}
