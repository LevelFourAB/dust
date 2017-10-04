package se.l4.dust.api;

import java.util.HashMap;
import java.util.Map;

public class DefaultContext
	implements Context
{
	private final Map<Object, Object> values;

	public DefaultContext()
	{
		values = new HashMap<Object, Object>();
	}

	@Override
	public void putValue(Object key, Object value)
	{
		values.put(key, value);
	}

	@Override
	public <T> T getValue(Object key)
	{
		return (T) values.get(key);
	}

}
