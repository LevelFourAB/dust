package se.l4.dust.core.internal.conversion.standard;

import se.l4.dust.api.conversion.Conversion;

/**
 * Conversion between a number and a long.
 *
 * @author Andreas Holstenson
 *
 */
public class NumberLongConversion
	implements Conversion<Number, Long>
{
	@Override
	public Long convert(Number in)
	{
		return in.longValue();
	}
}
