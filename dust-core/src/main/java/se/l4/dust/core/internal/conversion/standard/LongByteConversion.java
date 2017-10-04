package se.l4.dust.core.internal.conversion.standard;

import se.l4.dust.api.conversion.NonGenericConversion;

public class LongByteConversion
	implements NonGenericConversion<Long, Byte>
{

	@Override
	public Byte convert(Long in)
	{
		return in.byteValue();
	}

	@Override
	public Class<Long> getInput()
	{
		return Long.class;
	}

	@Override
	public Class<Byte> getOutput()
	{
		return Byte.class;
	}

}
