package se.l4.dust.core.internal.conversion.standard;

import se.l4.dust.api.conversion.NonGenericConversion;

public class VoidBooleanConversion
	implements NonGenericConversion<Void, Boolean>
{

	public Boolean convert(Void in)
	{
		return false;
	}

	public Class<Void> getInput()
	{
		return void.class;
	}

	public Class<Boolean> getOutput()
	{
		return Boolean.class;
	}

}
