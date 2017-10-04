package se.l4.dust.api.conversion;

/**
 * Conversion between two types, used by the {@link TypeConverter} to perform
 * actual type conversions. To use this class the implementor must specify
 * the types properly as generic parameters to the class. If that is not
 * possible use {@link NonGenericConversion}.
 *
 * @author Andreas Holstenson
 *
 * @param <I>
 * 		input type
 * @param <O>
 * 		output type
 */
public interface Conversion<I, O>
{
	/**
	 * Convert the given input.
	 *
	 * @param in
	 * 		input
	 * @return
	 * 		converted output
	 * @throws ConversionException
	 * 		if unable to convert input
	 */
	O convert(I in);
}
