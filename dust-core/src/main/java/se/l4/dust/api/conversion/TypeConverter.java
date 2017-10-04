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
	 * Add a conversion between two types.
	 *
	 * @param in
	 * @param out
	 * @param conversion
	 */
	<I, O> void add(Class<I> in, Class<O> out, Conversion<I, O> conversion);

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

	/**
	 * Get a conversion between the given input and output. If unsupported this
	 * will throw {@link ConversionException}.
	 *
	 * @param in
	 * @param out
	 * @return
	 */
	<I, O> NonGenericConversion<I, O> getConversion(Class<I> in, Class<O> out);

	/**
	 * Get a conversion between the given input and output if possible, or
	 * return a dynamic converter that converts to the output.
	 *
	 * @param in
	 * @param out
	 * @return
	 */
	<I, O> NonGenericConversion<Object, O> getDynamicConversion(Class<I> in, Class<O> out);

	/**
	 * Create a conversion that converts any object to the specific type.
	 *
	 * @param out
	 * @return
	 */
	<T> NonGenericConversion<Object, T> createDynamicConversionTo(Class<T> out);

	/**
	 * Get a conversion that will just return {@code null}.
	 *
	 * @return
	 */
	<I, O> Conversion<I, O> nullConversion();
}
