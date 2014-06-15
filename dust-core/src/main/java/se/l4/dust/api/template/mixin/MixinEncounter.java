package se.l4.dust.api.template.mixin;

import java.util.List;

import se.l4.dust.api.Value;
import se.l4.dust.api.template.Emittable;
import se.l4.dust.api.template.TemplateEncounter;
import se.l4.dust.api.template.dom.Content;

/**
 * Encounter for mixing during template parsing. The encounter is triggered
 * when a mixin can be created, the mixin can then use the encounter to
 * modify the parsed template.
 * 
 * @author Andreas Holstenson
 *
 */
public interface MixinEncounter
	extends TemplateEncounter
{
	/**
	 * Wrap the current element with the specified wrapper.
	 * 
	 * @param wrapper
	 */
	void wrap(ElementWrapper wrapper);
	
	/**
	 * Insert content at the beginning of the current element.
	 * 
	 * @param content
	 */
	void prepend(Emittable... content);
	
	/**
	 * Insert content at the beginning of the current element, see
	 * {@link #prepend(Content...)}.
	 * 
	 * @param content
	 */
	void prepend(List<? extends Emittable> content);
	
	/**
	 * Insert content at the end of the current element.
	 * 
	 * @param content
	 */
	void append(Emittable... content);
	
	/**
	 * Insert content at the end of the current element, see
	 * {@link #append(Content...)}.
	 * 
	 * @param content
	 */
	void append(List<? extends Emittable> content);
	
	/**
	 * Set the value of a given attribute.
	 * 
	 * @param attribute
	 * @param content
	 */
	void setAttribute(String attribute, Value<?> content);
}
