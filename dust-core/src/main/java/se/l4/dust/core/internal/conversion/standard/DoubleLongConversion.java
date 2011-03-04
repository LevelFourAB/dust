package se.l4.dust.core.internal.conversion.standard;

import se.l4.dust.api.conversion.NonGenericConversion;

public class DoubleLongConversion
	implements NonGenericConversion<Double, Long>
{

	public Long convert(Double in)
	{
		return in.longValue();
	}

	public Class<Double> getInput()
	{
		return Double.class;
	}

	public Class<Long> getOutput()
	{
		return Long.class;
	}

}
