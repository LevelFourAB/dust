package se.l4.dust.core.internal.conversion.standard;

import se.l4.dust.api.conversion.NonGenericConversion;

public class LongIntegerConversion
	implements NonGenericConversion<Long, Integer>
{

	public Integer convert(Long in)
	{
		return in.intValue();
	}

	public Class<Long> getInput()
	{
		return Long.class;
	}

	public Class<Integer> getOutput()
	{
		return Integer.class;
	}

}
