package se.l4.dust.validation;

import java.util.Set;

import javax.validation.ConstraintViolation;

import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.api.template.TemplateException;
import se.l4.dust.api.template.dom.Attribute;
import se.l4.dust.api.template.mixin.ElementEncounter;
import se.l4.dust.api.template.mixin.ElementWrapper;
import se.l4.dust.api.template.mixin.MixinEncounter;
import se.l4.dust.api.template.mixin.TemplateMixin;

/**
 * Mixin for violations. This mixin is responsible for binding violations to
 * the current context. These are later used by the other mixins and exposed
 * properties.
 * 
 * @author Andreas Holstenson
 *
 */
public class ViolationsMixin
	implements TemplateMixin
{

	@Override
	public void element(MixinEncounter encounter)
	{
		Attribute<?> errors = encounter.getAttribute(ValidationModule.NAMESPACE, ValidationModule.TPL_VIOLATIONS);
		encounter.wrap(new Wrapper(errors));
	}

	private static class Wrapper
		implements ElementWrapper
	{
		private final Attribute<?> errors;

		public Wrapper(Attribute<?> errors)
		{
			this.errors = errors;
		}

		@Override
		public void beforeElement(ElementEncounter encounter)
		{
			RenderingContext ctx = encounter.getContext();
			
			Object value = errors.get(
				ctx, 
				encounter.getObject()
			);
			
			if(value != null && ! (value instanceof Set))
			{
				throw new TemplateException("Errors should be of type " + 
					Set.class.getSimpleName() 
					+ " with entries of type " 
					+ ConstraintViolation.class.getSimpleName());
			}
			
			ctx.putValue(ValidationModule.CTX_ERRORS, value);
		}

		@Override
		public void afterElement(ElementEncounter encounter)
		{
			encounter.getContext().putValue(ValidationModule.CTX_ERRORS, null);
		}
		
	}
}
