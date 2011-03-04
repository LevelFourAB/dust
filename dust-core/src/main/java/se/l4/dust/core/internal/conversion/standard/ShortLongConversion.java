package se.l4.dust.core.internal.conversion.standard;

import se.l4.dust.api.conversion.NonGenericConversion;

public class ShortLongConversion
	implements NonGenericConversion<Short, Long>
{

	public Long convert(Short in)
	{
		return in.longValue();
	}

	public Class<Short> getInput()
	{
		return Short.class;
	}

	public Class<Long> getOutput()
	{
		return Long.class;
	}

}
