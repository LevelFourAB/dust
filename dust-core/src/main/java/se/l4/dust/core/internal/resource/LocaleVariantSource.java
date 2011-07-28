package se.l4.dust.core.internal.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import se.l4.dust.api.Context;
import se.l4.dust.api.resource.variant.ResourceVariant;
import se.l4.dust.api.resource.variant.ResourceVariantSource;

/**
 * Source of variants based on locale. This will produce variants that may be
 * tried with decreasing importance.
 * 
 * @author Andreas Holstenson
 *
 */
public class LocaleVariantSource
	implements ResourceVariantSource
{
	public static final String LOCALE = "locale";
	
	public List<ResourceVariant> getVariants(Context ctx)
	{
		Object value = ctx.getValue(LOCALE);
		if(value instanceof Locale)
		{
			Locale locale = (Locale) value;
			
			List<ResourceVariant> result = new ArrayList<ResourceVariant>(3);
			
			// With variant (three parts)
			if(locale.getVariant() != null && ! locale.getVariant().isEmpty())
			{
				String identifier = locale.getLanguage()
					+ "_" + locale.getCountry()
					+ "_" + locale.getVariant();
				
				result.add(new LocaleVariant(locale, identifier));
			}
			
			// With country (two parts)
			if(locale.getCountry() != null && ! locale.getCountry().isEmpty())
			{
				String identifier = locale.getLanguage()
					+ "_" + locale.getCountry();
			
				result.add(new LocaleVariant(locale, identifier));
			}
			
			// Only country part
			result.add(new LocaleVariant(locale, locale.getLanguage()));
			
			return result;
		}
		
		return Collections.emptyList();
	}
	
	public Object getCacheValue(Context ctx)
	{
		return ctx.getValue(LOCALE);
	}
	
	private static class LocaleVariant
		implements ResourceVariant
	{
		private final Locale locale;
		private final String identifier;

		public LocaleVariant(Locale locale, String identifier)
		{
			this.locale = locale;
			this.identifier = identifier;
		}
		
		public Object getCacheValue()
		{
			return locale;
		}
		
		public String getIdentifier()
		{
			return identifier;
		}
	}

}
