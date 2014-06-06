package se.l4.dust.validation;

import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.api.template.dom.Attribute;
import se.l4.dust.api.template.mixin.ElementEncounter;
import se.l4.dust.api.template.mixin.ElementWrapper;
import se.l4.dust.api.template.mixin.MixinEncounter;
import se.l4.dust.api.template.mixin.TemplateMixin;

/**
 * Mixin for exposing the current field name (around any other element).
 * 
 * @author Andreas Holstenson
 *
 */
public class FieldMixin
	implements TemplateMixin
{

	@Override
	public void element(MixinEncounter encounter)
	{
		Attribute field = encounter.getAttribute(ValidationModule.NAMESPACE, ValidationModule.TPL_FIELD);
		encounter.wrap(new Wrapper(field));
	}

	private static class Wrapper
		implements ElementWrapper
	{
		private final Attribute field;

		public Wrapper(Attribute field)
		{
			this.field = field;
		}

		@Override
		public void beforeElement(ElementEncounter encounter)
		{
			RenderingContext ctx = encounter.getContext();
			
			Object value = field.getValue(
				ctx, 
				encounter.getObject()
			);
			
			ctx.putValue(ValidationModule.CTX_FIELD, value);
		}

		@Override
		public void afterElement(ElementEncounter encounter)
		{
			RenderingContext ctx = encounter.getContext();
			ctx.putValue(ValidationModule.CTX_FIELD, null);
			ctx.putValue(ValidationModule.CTX_VIOLATION, null);
		}
		
	}

}
