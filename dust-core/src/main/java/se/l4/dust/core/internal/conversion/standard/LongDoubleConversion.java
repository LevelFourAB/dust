package se.l4.dust.core.internal.conversion.standard;

import se.l4.dust.api.conversion.NonGenericConversion;

public class LongDoubleConversion
	implements NonGenericConversion<Long, Double>
{

	public Double convert(Long in)
	{
		return in.doubleValue();
	}

	public Class<Long> getInput()
	{
		return Long.class;
	}

	public Class<Double> getOutput()
	{
		return Double.class;
	}

}
