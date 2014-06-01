package se.l4.dust.core.internal.template.components;

import se.l4.dust.api.template.fragment.FragmentEncounter;
import se.l4.dust.api.template.fragment.TemplateFragment;

public class HolderComponent
	implements TemplateFragment
{
	public HolderComponent()
	{
	}
	
	@Override
	public void build(FragmentEncounter encounter)
	{
		// Just skip this element
		encounter.replaceWith(encounter.getBody());
	}
}
