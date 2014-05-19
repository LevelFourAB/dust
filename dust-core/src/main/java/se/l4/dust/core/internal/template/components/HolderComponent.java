package se.l4.dust.core.internal.template.components;

import se.l4.dust.api.template.spi.FragmentEncounter;
import se.l4.dust.api.template.spi.TemplateFragment;

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
