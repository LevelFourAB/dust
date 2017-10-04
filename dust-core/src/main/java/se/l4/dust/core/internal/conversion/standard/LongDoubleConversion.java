package se.l4.dust.core.internal.conversion.standard;

import se.l4.dust.api.conversion.NonGenericConversion;

public class LongDoubleConversion
	implements NonGenericConversion<Long, Double>
{

	@Override
	public Double convert(Long in)
	{
		return in.doubleValue();
	}

	@Override
	public Class<Long> getInput()
	{
		return Long.class;
	}

	@Override
	public Class<Double> getOutput()
	{
		return Double.class;
	}

}
