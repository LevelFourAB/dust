package se.l4.dust.core.internal.conversion.standard;

import se.l4.dust.api.conversion.NonGenericConversion;

public class BooleanStringConversion
	implements NonGenericConversion<Boolean, String>
{

	public String convert(Boolean in)
	{
		return in.toString();
	}

	public Class<Boolean> getInput()
	{
		return Boolean.class;
	}

	public Class<String> getOutput()
	{
		return String.class;
	}

}
