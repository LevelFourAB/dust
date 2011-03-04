package se.l4.dust.core.internal.conversion.standard;

import se.l4.dust.api.conversion.NonGenericConversion;

public class ByteLongConversion
	implements NonGenericConversion<Byte, Long>
{

	public Long convert(Byte in)
	{
		return in.longValue();
	}

	public Class<Byte> getInput()
	{
		return Byte.class;
	}

	public Class<Long> getOutput()
	{
		return Long.class;
	}

}
