package se.l4.dust.core.internal.conversion.standard;

import se.l4.dust.api.conversion.NonGenericConversion;

public class DoubleLongConversion
	implements NonGenericConversion<Double, Long>
{

	@Override
	public Long convert(Double in)
	{
		return in.longValue();
	}

	@Override
	public Class<Double> getInput()
	{
		return Double.class;
	}

	@Override
	public Class<Long> getOutput()
	{
		return Long.class;
	}

}
