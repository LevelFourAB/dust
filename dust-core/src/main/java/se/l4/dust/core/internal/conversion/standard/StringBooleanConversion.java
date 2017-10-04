package se.l4.dust.core.internal.conversion.standard;

import se.l4.dust.api.conversion.ConversionException;
import se.l4.dust.api.conversion.NonGenericConversion;

public class StringBooleanConversion
	implements NonGenericConversion<String, Boolean>
{

	public Boolean convert(String in)
	{
		in = in.trim().toLowerCase();

		if(in.equals("true") || in.equals("on") || in.equals("1"))
		{
			return true;
		}
		else if(in.equals("false") || in.equals("off") || in.equals("0"))
		{
			return false;
		}

		throw new ConversionException("Invalid boolean string: " + in);
	}

	public Class<String> getInput()
	{
		return String.class;
	}

	public Class<Boolean> getOutput()
	{
		return Boolean.class;
	}

}
