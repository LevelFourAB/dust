package se.l4.dust.core.internal.conversion.standard;

import se.l4.dust.api.conversion.NonGenericConversion;

public class DoubleStringConversion
	implements NonGenericConversion<Double, String>
{

	@Override
	public String convert(Double in)
	{
		return in.toString();
	}

	@Override
	public Class<Double> getInput()
	{
		return Double.class;
	}

	@Override
	public Class<String> getOutput()
	{
		return String.class;
	}

}
