package se.l4.dust.core.internal.conversion.standard;

import se.l4.dust.api.conversion.ConversionException;
import se.l4.dust.api.conversion.NonGenericConversion;

public class StringDoubleConversion
	implements NonGenericConversion<String, Double>
{

	public Double convert(String in)
	{
		try
		{
			return Double.parseDouble(in);
		}
		catch(NumberFormatException e)
		{
			throw new ConversionException("Invalid double; " + e.getMessage(), e);
		}
	}

	public Class<String> getInput()
	{
		return String.class;
	}

	public Class<Double> getOutput()
	{
		return Double.class;
	}

}
