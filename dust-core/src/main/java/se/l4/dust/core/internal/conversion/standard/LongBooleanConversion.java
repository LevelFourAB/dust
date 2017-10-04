package se.l4.dust.core.internal.conversion.standard;

import se.l4.dust.api.conversion.NonGenericConversion;

public class LongBooleanConversion
	implements NonGenericConversion<Long, Boolean>
{

	@Override
	public Boolean convert(Long in)
	{
		return in.longValue() == 1 ? true : false;
	}

	@Override
	public Class<Long> getInput()
	{
		return Long.class;
	}

	@Override
	public Class<Boolean> getOutput()
	{
		return Boolean.class;
	}

}
