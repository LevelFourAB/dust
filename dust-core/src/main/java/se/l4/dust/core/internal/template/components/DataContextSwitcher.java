package se.l4.dust.core.internal.template.components;

import java.io.IOException;

import se.l4.dust.api.template.Emittable;
import se.l4.dust.api.template.TemplateEmitter;
import se.l4.dust.api.template.TemplateOutputStream;
import se.l4.dust.core.internal.template.TemplateEmitterImpl;

public class DataContextSwitcher
	implements Emittable
{
	private final Integer id;
	private final Emittable[] content;

	public DataContextSwitcher(Integer id, Emittable[] content)
	{
		this.id = id;
		this.content = content;
	}

	@Override
	public void emit(TemplateEmitter emitter, TemplateOutputStream out)
		throws IOException
	{
		TemplateEmitterImpl emitterImpl = (TemplateEmitterImpl) emitter;
		Integer old = emitterImpl.switchData(id);
		Integer oldC = emitterImpl.switchComponent(id);

		emitterImpl.emit(content);

		emitterImpl.switchComponent(oldC);
		emitterImpl.switchData(old);
	}

	@Override
	public String toString()
	{
		return "DataSwitch[id=" + id + "]";
	}
}
