package se.l4.dust.core.internal.conversion.standard;

import se.l4.dust.api.conversion.NonGenericConversion;

public class VoidDoubleConversion
	implements NonGenericConversion<Void, Double>
{

	public Double convert(Void in)
	{
		return 0.0;
	}

	public Class<Void> getInput()
	{
		return void.class;
	}

	public Class<Double> getOutput()
	{
		return Double.class;
	}

}
