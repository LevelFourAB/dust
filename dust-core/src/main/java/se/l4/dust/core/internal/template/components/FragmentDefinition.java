package se.l4.dust.core.internal.template.components;

import se.l4.dust.api.template.dom.Element.Attribute;
import se.l4.dust.api.template.spi.FragmentEncounter;
import se.l4.dust.api.template.spi.TemplateFragment;

/**
 * Definition of a fragment.
 * 
 * @author Andreas Holstenson
 *
 */
public class FragmentDefinition
	implements TemplateFragment
{
	public FragmentDefinition()
	{
	}
	
	@Override
	public void build(FragmentEncounter encounter)
	{
		Attribute attribute = encounter.getAttribute("id");
		String id = attribute.getStringValue();
		
		encounter.builder().putValue("fragment|" + id, encounter.getBody());
	}
}
