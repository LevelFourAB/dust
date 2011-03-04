package se.l4.dust.core.internal.conversion.standard;

import se.l4.dust.api.conversion.NonGenericConversion;

public class FloatDoubleConversion
	implements NonGenericConversion<Float, Double>
{

	public Double convert(Float in)
	{
		return in.doubleValue();
	}

	public Class<Float> getInput()
	{
		return Float.class;
	}

	public Class<Double> getOutput()
	{
		return Double.class;
	}

}
