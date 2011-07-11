package se.l4.dust.api.conversion;


/**
 * Type converter for performing conversions between arbitrary types. This is
 * used to support different types in templates.
 * 
 * @author Andreas Holstenson
 *
 */
public interface TypeConverter
{
	/**
	 * Convert the given input to another type.
	 * 
	 * @param <T>
	 * 		type of output
	 * @param in
	 * 		value to convert (input)
	 * @param output
	 * 		output type
	 * @return
	 * 		converted value
	 * @throws ConversionException
	 * 		if unable to convert
	 */
	<T> T convert(Object in, Class<T> output);

	/**
	 * Add a conversion between two types.
	 * 
	 * @param conversion
	 * 		conversion
	 */
	void add(Conversion<?, ?> conversion);
	
	/**
	 * Check if a conversion is supported.
	 * 
	 * @param in
	 * @param out
	 * @return
	 */
	boolean canConvertBetween(Class<?> in, Class<?> out);

	/**
	 * Check if a conversion is supported.
	 * 
	 * @param in
	 * @param out
	 * @return
	 */
	boolean canConvertBetween(Object in, Class<?> out);
}
