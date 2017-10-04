package se.l4.dust.core.internal.conversion.standard;

import se.l4.dust.api.conversion.NonGenericConversion;

public class LongStringConversion
	implements NonGenericConversion<Long, String>
{

	@Override
	public String convert(Long in)
	{
		return in.toString();
	}

	@Override
	public Class<Long> getInput()
	{
		return Long.class;
	}

	@Override
	public Class<String> getOutput()
	{
		return String.class;
	}

}
