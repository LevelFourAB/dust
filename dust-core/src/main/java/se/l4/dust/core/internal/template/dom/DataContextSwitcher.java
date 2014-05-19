package se.l4.dust.core.internal.template.dom;

import java.io.IOException;

import se.l4.dust.api.template.Emittable;
import se.l4.dust.api.template.TemplateEmitter;
import se.l4.dust.api.template.dom.Content;
import se.l4.dust.api.template.spi.TemplateOutputStream;

public class DataContextSwitcher
	implements Emittable
{
	private final Integer id;
	private final Content[] content;

	public DataContextSwitcher(Integer id, Content[] content)
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
		
		for(Content c : content)
		{
			emitterImpl.emit(out, c);
		}
		
		emitterImpl.switchData(old);
	}
	
	@Override
	public String toString()
	{
		return "DataSwitch[id=" + id + "]";
	}
}
