package se.l4.dust.api.template;

import java.io.IOException;

import se.l4.dust.api.Value;
import se.l4.dust.api.conversion.Conversion;

public class EmittableValue<T>
	implements Emittable
{
	private final Value<T> value;
	private final Conversion<T, String> conversion;

	public EmittableValue(Value<T> value, Conversion<T, String> conversion)
	{
		this.value = value;
		this.conversion = conversion;
	}

	@Override
	public void emit(TemplateEmitter emitter, TemplateOutputStream output)
		throws IOException
	{
		T data = value.get(emitter.getContext(), emitter.getObject());
		output.text(conversion.convert(data));
	}
}
