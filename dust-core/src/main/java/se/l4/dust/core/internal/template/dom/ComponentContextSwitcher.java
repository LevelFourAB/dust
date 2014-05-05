package se.l4.dust.core.internal.template.dom;

import java.io.IOException;

import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.api.template.dom.Content;
import se.l4.dust.api.template.spi.TemplateOutputStream;
import se.l4.dust.core.internal.template.components.EmittableComponent;

public class ComponentContextSwitcher
	extends EmittableComponent
{
	private final Integer id;

	public ComponentContextSwitcher(Integer id)
	{
		super("ContextSwitch", null);
		this.id = id;
	}
	
	@Override
	public String getName()
	{
		return super.getName() + "[" + id + "]";
	}
	
	@Override
	public Content doCopy()
	{
		return new ComponentContextSwitcher(id);
	}
	
	@Override
	public void emit(Emitter emitter, RenderingContext ctx,
			TemplateOutputStream out) throws IOException
	{
		Integer old = emitter.switchComponent(id);
		
		for(Content c : getRawContents())
		{
			emitter.emit(out, c);
		}
		
		emitter.switchComponent(old);
	}
	
	@Override
	public String toString()
	{
		return "ComponentSwitch[id=" + id + ", identity=" + System.identityHashCode(this) + "]";
	}
}
