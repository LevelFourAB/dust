package se.l4.dust.core.internal.conversion.standard;

import se.l4.dust.api.conversion.NonGenericConversion;

public class LongStringConversion
	implements NonGenericConversion<Long, String>
{

	public String convert(Long in)
	{
		return in.toString();
	}

	public Class<Long> getInput()
	{
		return Long.class;
	}

	public Class<String> getOutput()
	{
		return String.class;
	}

}
