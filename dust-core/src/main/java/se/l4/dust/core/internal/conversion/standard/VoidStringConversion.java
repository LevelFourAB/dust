package se.l4.dust.core.internal.conversion.standard;

import se.l4.dust.api.conversion.NonGenericConversion;

public class VoidStringConversion
	implements NonGenericConversion<Void, String>
{

	public String convert(Void in)
	{
		return null;
	}

	public Class<Void> getInput()
	{
		return void.class;
	}

	public Class<String> getOutput()
	{
		return String.class;
	}

}
