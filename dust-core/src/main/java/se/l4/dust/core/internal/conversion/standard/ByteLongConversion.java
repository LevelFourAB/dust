package se.l4.dust.core.internal.conversion.standard;

import se.l4.dust.api.conversion.NonGenericConversion;

public class ByteLongConversion
	implements NonGenericConversion<Byte, Long>
{

	@Override
	public Long convert(Byte in)
	{
		return in.longValue();
	}

	@Override
	public Class<Byte> getInput()
	{
		return Byte.class;
	}

	@Override
	public Class<Long> getOutput()
	{
		return Long.class;
	}

}
