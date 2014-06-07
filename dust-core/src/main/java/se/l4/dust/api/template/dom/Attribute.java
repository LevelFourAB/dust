package se.l4.dust.api.template.dom;

import se.l4.dust.api.conversion.TypeConverter;
import se.l4.dust.api.template.Value;

public interface Attribute<T>
	extends Value<T>
{
	static final String ATTR_EMIT = "##emit";
	static final String ATTR_SKIP = "##skip";
	
	String getName();

	String getStringValue();
	
	<N> Attribute<N> bindVia(TypeConverter converter, Class<N> output);
}