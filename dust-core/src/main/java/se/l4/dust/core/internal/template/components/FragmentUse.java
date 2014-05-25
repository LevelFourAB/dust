package se.l4.dust.core.internal.template.components;

import java.util.ArrayList;
import java.util.List;

import se.l4.dust.api.template.TemplateException;
import se.l4.dust.api.template.dom.Content;
import se.l4.dust.api.template.dom.Element.Attribute;
import se.l4.dust.api.template.spi.FragmentEncounter;
import se.l4.dust.api.template.spi.TemplateFragment;

/**
 * Usage of a previously defined fragment.
 * 
 * @author Andreas Holstenson
 *
 */
public class FragmentUse
	implements TemplateFragment
{

	@Override
	public void build(FragmentEncounter encounter)
	{
		Attribute attribute = encounter.getAttribute("id");
		if(attribute == null)
		{
			throw new TemplateException("id attribute is required for fragment usage");
		}
		
		String id = attribute.getStringValue();
		Content[] content = encounter.builder().getValue("fragment|" + id);
		if(content == null)
		{
			throw new TemplateException("The fragment `" + id + "` must be defined before it can be used");
		}
		
		List<Content> copy = new ArrayList<Content>();
		for(Content c : content)
		{
			copy.add(c.deepCopy());
		}
		encounter.builder().addContent(copy);
	}

}
