package se.l4.dust.core.internal.conversion.standard;

import se.l4.dust.api.conversion.NonGenericConversion;

public class VoidBooleanConversion
	implements NonGenericConversion<Void, Boolean>
{

	@Override
	public Boolean convert(Void in)
	{
		return false;
	}

	@Override
	public Class<Void> getInput()
	{
		return void.class;
	}

	@Override
	public Class<Boolean> getOutput()
	{
		return Boolean.class;
	}

}
