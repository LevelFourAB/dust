package se.l4.dust.core.internal.template.components;

import se.l4.dust.api.template.fragment.FragmentEncounter;
import se.l4.dust.api.template.fragment.TemplateFragment;

public class ParameterComponent
	implements TemplateFragment
{
	public ParameterComponent()
	{
	}
	
	@Override
	public void build(FragmentEncounter encounter)
	{
		String name = encounter.getAttribute("name").getStringValue();
		encounter.addParameter(name, encounter.getScopedBody());
	}
}
