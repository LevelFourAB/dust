package se.l4.dust.api.template.mixin;

import se.l4.dust.api.template.Templates;

/**
 * Mixin handler for a template. Called for every instance where the
 * mixin should be applied.
 * 
 * <p>
 * Mixins are registered via {@link Templates} and trigger either on a
 * {@link Templates.TemplateNamespace#addMixin(String, TemplateMixin) specific attribute}
 * or on {@link Templates.TemplateNamespace#addMixin(TemplateMixin) any attribute}
 * 
 * @author Andreas Holstenson
 *
 */
public interface TemplateMixin
{
	/**
	 * Called when an element matches an attribute for this mixin. The mixin
	 * should modify the template as needed here.
	 * 
	 * @param encounter
	 */
	void element(MixinEncounter encounter);
}
