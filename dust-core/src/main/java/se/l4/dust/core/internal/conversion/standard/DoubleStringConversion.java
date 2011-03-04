package se.l4.dust.core.internal.conversion.standard;

import se.l4.dust.api.conversion.NonGenericConversion;

public class DoubleStringConversion
	implements NonGenericConversion<Double, String>
{

	public String convert(Double in)
	{
		return in.toString();
	}

	public Class<Double> getInput()
	{
		return Double.class;
	}

	public Class<String> getOutput()
	{
		return String.class;
	}

}
