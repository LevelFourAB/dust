package se.l4.dust.core.internal.template.components;

import se.l4.dust.api.template.Emittable;
import se.l4.dust.api.template.TemplateException;
import se.l4.dust.api.template.dom.Attribute;
import se.l4.dust.api.template.fragment.FragmentEncounter;
import se.l4.dust.api.template.fragment.TemplateFragment;

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
		Attribute<?> attribute = encounter.getAttribute("id", true);

		String id = attribute.getStringValue();
		Emittable[] content = encounter.getValue("fragment|" + id);
		if(content == null)
		{
			throw new TemplateException("The fragment `" + id + "` must be defined before it can be used");
		}

		encounter.replaceWith(content);
	}
}
