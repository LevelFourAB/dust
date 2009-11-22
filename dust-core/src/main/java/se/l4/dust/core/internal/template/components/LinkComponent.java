package se.l4.dust.core.internal.template.components;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.UriBuilder;

import org.jdom.Attribute;
import org.jdom.Content;
import org.jdom.JDOMException;

import com.google.inject.Singleton;

import se.l4.dust.core.internal.template.dom.TemplateAttribute;
import se.l4.dust.core.internal.template.dom.TemplateComponent;
import se.l4.dust.core.internal.template.dom.TemplateEmitter;
import se.l4.dust.core.template.TemplateModule;
import se.l4.dust.dom.Element;

@Singleton
public class LinkComponent
	extends TemplateComponent
{
	public LinkComponent()
	{
		super("pagelink", TemplateModule.COMMON);
	}
	
	@Override
	public void process(
			TemplateEmitter emitter, 
			Element parent, 
			Object root,
			TemplateComponent lastComponent, 
			Object previousRoot)
		throws JDOMException
	{
		Map<String, Object> values = new HashMap<String, Object>();

		Element out = new Element("a");
		String uri = null;
		for(Attribute a : getAttributes())
		{
			TemplateAttribute ta = (TemplateAttribute) a;
			
			if(getNamespace().equals(a.getNamespace()))
			{
				if(a.getName().equals("page"))
				{
					// TODO: Conversion
					uri = (String) ta.getValue(root);
				}
				else
				{
					values.put(a.getName(), ta.getValue(root));
				}
			}
			else
			{
				out.setAttribute(a.getName(), (String) ta.getValue(root)); 
			}
		}
		
		URI href = UriBuilder.fromPath(uri).buildFromMap(values);
		out.setAttribute("href", href.toString());
		parent.addContent(out);
		
		for(Content c : getContent())
		{
			emitter.process(root, out, c, lastComponent, previousRoot);
		}
	}

}
