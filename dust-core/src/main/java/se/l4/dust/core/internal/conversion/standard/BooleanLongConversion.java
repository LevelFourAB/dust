package se.l4.dust.core.internal.conversion.standard;

import se.l4.dust.api.conversion.NonGenericConversion;

public class BooleanLongConversion
	implements NonGenericConversion<Boolean, Long>
{

	@Override
	public Long convert(Boolean in)
	{
		return in.booleanValue() ? 1l : 0l;
	}

	@Override
	public Class<Boolean> getInput()
	{
		return Boolean.class;
	}

	@Override
	public Class<Long> getOutput()
	{
		return Long.class;
	}

}
