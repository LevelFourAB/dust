package se.l4.dust.core.internal.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import se.l4.dust.api.Context;
import se.l4.dust.api.resource.variant.ResourceVariant;
import se.l4.dust.api.resource.variant.ResourceVariantSource;

import com.google.common.base.Objects;

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
	public List<ResourceVariant> getVariants(Context ctx)
	{
		Object value = ctx.getValue(ResourceVariant.LOCALE);
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
		return ctx.getValue(ResourceVariant.LOCALE);
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

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((identifier == null)
				? 0
				: identifier.hashCode());
			result = prime * result + ((locale == null)
				? 0
				: locale.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if(this == obj)
				return true;
			if(obj == null)
				return false;
			if(getClass() != obj.getClass())
				return false;
			LocaleVariant other = (LocaleVariant) obj;
			if(identifier == null)
			{
				if(other.identifier != null)
					return false;
			}
			else if(!identifier.equals(other.identifier))
				return false;
			if(locale == null)
			{
				if(other.locale != null)
					return false;
			}
			else if(!locale.equals(other.locale))
				return false;
			return true;
		}
		
		@Override
		public String toString()
		{
			return Objects.toStringHelper(this)
				.add("id", identifier)
				.add("locale", locale)
				.toString();
		}
	}

}
