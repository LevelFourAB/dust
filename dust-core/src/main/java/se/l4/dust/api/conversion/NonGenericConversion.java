package se.l4.dust.api.conversion;

/**
 * Conversion between two types, used by the {@link TypeConverter} to perform
 * actual type conversions. If possible use {@link Conversion} instead.
 *
 * @author Andreas Holstenson
 *
 * @param <I>
 * 		input type
 * @param <O>
 * 		output type
 */
public interface NonGenericConversion<I, O>
	extends Conversion<I, O>
{
	/**
	 * Get class of input (should match {@code <I>} parameter).
	 *
	 * @return
	 */
	Class<I> getInput();

	/**
	 * Get class of output (should match {@code <O>} parameter).
	 *
	 * @return
	 */
	Class<O> getOutput();
}
