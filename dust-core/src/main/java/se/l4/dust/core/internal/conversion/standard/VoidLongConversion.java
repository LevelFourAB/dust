package se.l4.dust.core.internal.conversion.standard;

import se.l4.dust.api.conversion.NonGenericConversion;

public class VoidLongConversion
	implements NonGenericConversion<Void, Long>
{

	@Override
	public Long convert(Void in)
	{
		return 0l;
	}

	@Override
	public Class<Void> getInput()
	{
		return void.class;
	}

	@Override
	public Class<Long> getOutput()
	{
		return Long.class;
	}

}
