package se.l4.dust.core.internal.template.components;

import se.l4.dust.api.template.dom.Attribute;
import se.l4.dust.api.template.fragment.FragmentEncounter;
import se.l4.dust.api.template.fragment.TemplateFragment;

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
		Attribute attribute = encounter.getAttribute("id", true);
		String id = attribute.getStringValue();
		
		encounter.putValue("fragment|" + id, encounter.getBody());
	}
}
