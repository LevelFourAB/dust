package se.l4.dust.core.internal.conversion.standard;

import se.l4.dust.api.conversion.NonGenericConversion;

public class FloatDoubleConversion
	implements NonGenericConversion<Float, Double>
{

	@Override
	public Double convert(Float in)
	{
		return in.doubleValue();
	}

	@Override
	public Class<Float> getInput()
	{
		return Float.class;
	}

	@Override
	public Class<Double> getOutput()
	{
		return Double.class;
	}

}
