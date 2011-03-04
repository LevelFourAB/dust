package se.l4.dust.core.internal.conversion.standard;

import se.l4.dust.api.conversion.NonGenericConversion;

public class LongShortConversion
	implements NonGenericConversion<Long, Short>
{

	public Short convert(Long in)
	{
		return in.shortValue();
	}

	public Class<Long> getInput()
	{
		return Long.class;
	}

	public Class<Short> getOutput()
	{
		return Short.class;
	}

}
