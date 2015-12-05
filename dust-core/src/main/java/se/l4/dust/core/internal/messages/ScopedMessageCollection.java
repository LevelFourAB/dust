package se.l4.dust.core.internal.messages;

import java.util.Set;

import se.l4.dust.api.messages.MessageCollection;

public class ScopedMessageCollection
	implements MessageCollection
{
	private final MessageCollection parent;
	private final String prefix;

	public ScopedMessageCollection(MessageCollection parent, String prefix)
	{
		this.parent = parent;
		this.prefix = prefix + '.';
	}

	@Override
	public String get(String property)
	{
		return parent.get(prefix + property);
	}

	@Override
	public Set<String> keys()
	{
		throw new UnsupportedOperationException();
	}

}
