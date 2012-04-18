package se.l4.dust.core.internal.conversion;

import se.l4.crayon.CrayonModule;
import se.l4.crayon.annotation.Contribution;
import se.l4.dust.api.conversion.TypeConverter;
import se.l4.dust.core.internal.conversion.standard.BooleanLongConversion;
import se.l4.dust.core.internal.conversion.standard.BooleanStringConversion;
import se.l4.dust.core.internal.conversion.standard.ByteLongConversion;
import se.l4.dust.core.internal.conversion.standard.DoubleFloatConversion;
import se.l4.dust.core.internal.conversion.standard.DoubleLongConversion;
import se.l4.dust.core.internal.conversion.standard.DoubleStringConversion;
import se.l4.dust.core.internal.conversion.standard.FloatDoubleConversion;
import se.l4.dust.core.internal.conversion.standard.IntegerLongConversion;
import se.l4.dust.core.internal.conversion.standard.LongBooleanConversion;
import se.l4.dust.core.internal.conversion.standard.LongByteConversion;
import se.l4.dust.core.internal.conversion.standard.LongDoubleConversion;
import se.l4.dust.core.internal.conversion.standard.LongIntegerConversion;
import se.l4.dust.core.internal.conversion.standard.LongShortConversion;
import se.l4.dust.core.internal.conversion.standard.LongStringConversion;
import se.l4.dust.core.internal.conversion.standard.NumberIntegerConversion;
import se.l4.dust.core.internal.conversion.standard.NumberLongConversion;
import se.l4.dust.core.internal.conversion.standard.ShortLongConversion;
import se.l4.dust.core.internal.conversion.standard.StringBooleanConversion;
import se.l4.dust.core.internal.conversion.standard.StringDoubleConversion;
import se.l4.dust.core.internal.conversion.standard.StringLongConversion;
import se.l4.dust.core.internal.conversion.standard.VoidBooleanConversion;
import se.l4.dust.core.internal.conversion.standard.VoidDoubleConversion;
import se.l4.dust.core.internal.conversion.standard.VoidLongConversion;
import se.l4.dust.core.internal.conversion.standard.VoidStringConversion;

import com.google.inject.Scopes;

/**
 * Module for type converter, contributes implementation and default
 * conversions.
 * 
 * @author Andreas Holstenson
 *
 */
public class ConversionModule
	extends CrayonModule
{
	@Override
	protected void configure()
	{
		bind(TypeConverter.class).
			to(DefaultTypeConverter.class)
			.in(Scopes.SINGLETON);
	}
	
	/**
	 * Register the default converters with {@link TypeConverter}. This
	 * includes conversion to/from strings and between numbers.
	 * 
	 * @param converter
	 */
	@Contribution
	public void contributeDefaultConversions(TypeConverter converter)
	{
		converter.add(new IntegerLongConversion());
		converter.add(new LongIntegerConversion());
		
		converter.add(new DoubleFloatConversion());
		converter.add(new FloatDoubleConversion());
		
		converter.add(new ShortLongConversion());
		converter.add(new LongShortConversion());
		
		converter.add(new ByteLongConversion());
		converter.add(new LongByteConversion());
		
		converter.add(new DoubleStringConversion());
		converter.add(new StringDoubleConversion());
		
		converter.add(new LongStringConversion());
		converter.add(new StringLongConversion());
		
		converter.add(new BooleanLongConversion());
		converter.add(new LongBooleanConversion());
		
		converter.add(new DoubleLongConversion());
		converter.add(new LongDoubleConversion());
		
		converter.add(new BooleanStringConversion());
		converter.add(new StringBooleanConversion());
		
		converter.add(new VoidDoubleConversion());
		converter.add(new VoidBooleanConversion());
		converter.add(new VoidLongConversion());
		converter.add(new VoidStringConversion());
		
		converter.add(new NumberLongConversion());
		converter.add(new NumberIntegerConversion());
	}
}
