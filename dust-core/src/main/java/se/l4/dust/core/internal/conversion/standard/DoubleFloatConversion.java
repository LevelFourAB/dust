package se.l4.dust.core.internal.conversion.standard;

import se.l4.dust.api.conversion.NonGenericConversion;

public class DoubleFloatConversion
	implements NonGenericConversion<Double, Float>
{

	@Override
	public Float convert(Double in)
	{
		return in.floatValue();
	}

	@Override
	public Class<Double> getInput()
	{
		return Double.class;
	}

	@Override
	public Class<Float> getOutput()
	{
		return Float.class;
	}

}
