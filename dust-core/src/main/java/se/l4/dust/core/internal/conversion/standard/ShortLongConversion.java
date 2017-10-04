package se.l4.dust.core.internal.conversion.standard;

import se.l4.dust.api.conversion.NonGenericConversion;

public class ShortLongConversion
	implements NonGenericConversion<Short, Long>
{

	@Override
	public Long convert(Short in)
	{
		return in.longValue();
	}

	@Override
	public Class<Short> getInput()
	{
		return Short.class;
	}

	@Override
	public Class<Long> getOutput()
	{
		return Long.class;
	}

}
