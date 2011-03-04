package se.l4.dust.core.internal.conversion.standard;

import se.l4.dust.api.conversion.NonGenericConversion;

public class IntegerLongConversion
	implements NonGenericConversion<Integer, Long>
{

	public Long convert(Integer in)
	{
		return in.longValue();
	}

	public Class<Integer> getInput()
	{
		return Integer.class;
	}

	public Class<Long> getOutput()
	{
		return Long.class;
	}

}
