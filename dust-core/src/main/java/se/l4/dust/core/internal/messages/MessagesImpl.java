package se.l4.dust.core.internal.messages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.inject.Singleton;

import se.l4.dust.api.Context;
import se.l4.dust.api.messages.MessageCollection;
import se.l4.dust.api.messages.MessageSource;
import se.l4.dust.api.messages.Messages;
import se.l4.dust.api.resource.ResourceLocation;
import se.l4.dust.api.template.TemplateException;

/**
 * Implementation of {@link Messages}.
 *
 * @author Andreas Holstenson
 *
 */
@Singleton
public class MessagesImpl
	implements Messages
{
	private final List<MessageSource> sources;

	public MessagesImpl()
	{
		sources = new CopyOnWriteArrayList<>();
	}

	@Override
	public void addSource(MessageSource source)
	{
		sources.add(source);
	}

	@Override
	public MessageCollection getMessages(Context context, ResourceLocation location)
	{
		Object key = location; // TODO: More accurate key?
		MessageCollection msgs = context.getValue(key);
		if(msgs != null)
		{
			return msgs;
		}

		List<MessageCollection> messages = new ArrayList<>();
		for(MessageSource s : sources)
		{
			try
			{
				MessageCollection m = s.load(context, location);
				if(m != null)
				{
					messages.add(m);
				}
			}
			catch(IOException e)
			{
				throw new TemplateException("Unable to load messages; " + e.getMessage(), e);
			}
		}

		if(messages.size() == 1)
		{
			msgs = messages.get(0);
		}
		else
		{
			msgs = new DelegatingMessages(messages);
		}

		context.putValue(key, msgs);
		return msgs;
	}
}
