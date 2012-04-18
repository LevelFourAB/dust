package se.l4.dust.core.internal.conversion.standard;

import se.l4.dust.api.conversion.Conversion;

/**
 * Conversion between a number and an integer.
 * 
 * @author Andreas Holstenson
 *
 */
public class NumberIntegerConversion
	implements Conversion<Number, Integer>
{
	@Override
	public Integer convert(Number in)
	{
		return in.intValue();
	}
}
