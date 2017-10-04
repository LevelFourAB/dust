package se.l4.dust.core.internal.conversion.standard;

import se.l4.dust.api.conversion.NonGenericConversion;

public class BooleanStringConversion
	implements NonGenericConversion<Boolean, String>
{

	@Override
	public String convert(Boolean in)
	{
		return in.toString();
	}

	@Override
	public Class<Boolean> getInput()
	{
		return Boolean.class;
	}

	@Override
	public Class<String> getOutput()
	{
		return String.class;
	}

}
