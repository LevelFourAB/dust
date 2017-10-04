package se.l4.dust.core.internal.conversion.standard;

import se.l4.dust.api.conversion.NonGenericConversion;

public class IntegerLongConversion
	implements NonGenericConversion<Integer, Long>
{

	@Override
	public Long convert(Integer in)
	{
		return in.longValue();
	}

	@Override
	public Class<Integer> getInput()
	{
		return Integer.class;
	}

	@Override
	public Class<Long> getOutput()
	{
		return Long.class;
	}

}
