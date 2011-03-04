package se.l4.dust.core.internal.conversion.standard;

import se.l4.dust.api.conversion.NonGenericConversion;

public class DoubleFloatConversion
	implements NonGenericConversion<Double, Float>
{

	public Float convert(Double in)
	{
		return in.floatValue();
	}

	public Class<Double> getInput()
	{
		return Double.class;
	}

	public Class<Float> getOutput()
	{
		return Float.class;
	}

}
