package se.l4.dust.api.template.fragment;

/**
 * Fragment as used within a template.
 *
 * @author Andreas Holstenson
 *
 */
public interface TemplateFragment
{
	/**
	 * Emit the contents of this fragment to the specified encounter.
	 *
	 * @param builder
	 */
	void build(FragmentEncounter encounter);
}
