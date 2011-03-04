package se.l4.dust.core.internal.conversion.standard;

import se.l4.dust.api.conversion.NonGenericConversion;

public class LongBooleanConversion
	implements NonGenericConversion<Long, Boolean>
{

	public Boolean convert(Long in)
	{
		return in.longValue() == 1 ? true : false;
	}

	public Class<Long> getInput()
	{
		return Long.class;
	}

	public Class<Boolean> getOutput()
	{
		return Boolean.class;
	}

}
