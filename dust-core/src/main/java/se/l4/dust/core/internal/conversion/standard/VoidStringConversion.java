package se.l4.dust.core.internal.conversion.standard;

import se.l4.dust.api.conversion.NonGenericConversion;

public class VoidStringConversion
	implements NonGenericConversion<Void, String>
{

	@Override
	public String convert(Void in)
	{
		return null;
	}

	@Override
	public Class<Void> getInput()
	{
		return void.class;
	}

	@Override
	public Class<String> getOutput()
	{
		return String.class;
	}

}
