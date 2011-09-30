package se.l4.dust.api.template.mixin;

import se.l4.dust.api.template.dom.Element;

/**
 * Encounter for mixing during template parsing. The encounter is triggered
 * when a mixin can be created, the mixin can then use the encounter to
 * modify the parsed template.
 * 
 * @author Andreas Holstenson
 *
 */
public interface MixinEncounter
{
	/**
	 * Get a specific attribute from the current element.
	 * 
	 * @param namespace
	 * @param name
	 * @return
	 */
	Element.Attribute getAttribute(String namespace, String name);
	
	/**
	 * Get a specific attribute from the current element.
	 * 
	 * @param name
	 * @return
	 */
	Element.Attribute getAttribute(String name);
	
	/**
	 * Wrap the current element with the specified wrapper.
	 * 
	 * @param wrapper
	 */
	void wrap(ElementWrapper wrapper);
}
