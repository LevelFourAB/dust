package se.l4.dust.core.internal.template.components;

import java.io.IOException;

import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.api.template.dom.Content;
import se.l4.dust.api.template.dom.Element.Attribute;
import se.l4.dust.api.template.spi.FragmentEncounter;
import se.l4.dust.api.template.spi.TemplateFragment;
import se.l4.dust.api.template.spi.TemplateOutputStream;
import se.l4.dust.core.internal.template.dom.Emitter;

public class IfComponent
	implements TemplateFragment
{
	@Override
	public void build(FragmentEncounter encounter)
	{
		Attribute test = encounter.getAttribute("test");
		
		IfComponent2 component = new IfComponent2(test, encounter.getBody());
		encounter.replaceWith(component);
	}
	
	
	public static class IfComponent2
		extends EmittableComponent
	{
		private final Attribute test;
		private final ParameterComponent elseParameter;

		public IfComponent2(Attribute test, Content[] content)
		{
			super("if", IfComponent2.class);
			this.test = test;
			this.setContents(content);
			elseParameter = getParameter("else", false);
		}

		@Override
		public void emit(Emitter emitter, RenderingContext ctx, TemplateOutputStream out)
			throws IOException
		{
			Object data = emitter.getObject();
			
			Object value = test.getValue(ctx, data);
			
			if(Boolean.TRUE.equals(value))
			{
				for(Content c : getRawContents())
				{
					if(c == elseParameter) continue;
				
					emitter.emit(out,  c);
				}
			}
			else if(elseParameter != null)
			{
				// Render the else
				for(Content c : elseParameter.getRawContents())
				{
					emitter.emit(out, c);
				}
			}
		}
		
		@Override
		public Content doCopy()
		{
			return new IfComponent2(test, this.getRawContents());
		}
	}
}
