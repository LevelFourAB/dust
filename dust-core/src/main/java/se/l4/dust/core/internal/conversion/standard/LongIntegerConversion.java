package se.l4.dust.core.internal.conversion.standard;

import se.l4.dust.api.conversion.NonGenericConversion;

public class LongIntegerConversion
	implements NonGenericConversion<Long, Integer>
{

	@Override
	public Integer convert(Long in)
	{
		return in.intValue();
	}

	@Override
	public Class<Long> getInput()
	{
		return Long.class;
	}

	@Override
	public Class<Integer> getOutput()
	{
		return Integer.class;
	}

}
