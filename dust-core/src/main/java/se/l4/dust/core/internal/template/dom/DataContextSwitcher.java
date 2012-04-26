package se.l4.dust.core.internal.template.dom;

import java.io.IOException;

import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.api.template.dom.Content;
import se.l4.dust.api.template.spi.TemplateOutputStream;
import se.l4.dust.core.internal.template.components.EmittableComponent;

public class DataContextSwitcher
	extends EmittableComponent
{
	private final Integer id;

	public DataContextSwitcher(Integer id)
	{
		super("switch", null);
		this.id = id;
	}

	@Override
	public Content copy()
	{
		return new DataContextSwitcher(id);
	}
	
	@Override
	public void emit(Emitter emitter, RenderingContext ctx, TemplateOutputStream out)
		throws IOException
	{
		Integer old = emitter.switchData(id);
		
		for(Content c : getRawContents())
		{
			emitter.emit(out, c);
		}
		
		emitter.switchData(old);
	}
	
	@Override
	public String toString()
	{
		return "DataSwitch[id=" + id + ", identity=" + System.identityHashCode(this) + "]";
	}
}
