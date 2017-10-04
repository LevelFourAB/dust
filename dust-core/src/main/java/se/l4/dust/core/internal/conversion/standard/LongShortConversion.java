package se.l4.dust.core.internal.conversion.standard;

import se.l4.dust.api.conversion.NonGenericConversion;

public class LongShortConversion
	implements NonGenericConversion<Long, Short>
{

	@Override
	public Short convert(Long in)
	{
		return in.shortValue();
	}

	@Override
	public Class<Long> getInput()
	{
		return Long.class;
	}

	@Override
	public Class<Short> getOutput()
	{
		return Short.class;
	}

}
