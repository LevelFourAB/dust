package se.l4.dust.core.internal.messages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import se.l4.dust.api.Context;
import se.l4.dust.api.messages.Messages;
import se.l4.dust.api.messages.MessageSource;
import se.l4.dust.api.messages.MessageCollection;
import se.l4.dust.api.template.TemplateException;

import com.google.inject.Singleton;

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
		sources = new CopyOnWriteArrayList<MessageSource>();
	}
	
	@Override
	public void addSource(MessageSource source)
	{
		sources.add(source);
	}

	@Override
	public MessageCollection getMessages(Context context, String url)
	{
		String key = url;
		MessageCollection msgs = context.getValue(key);
		if(msgs != null)
		{
			return msgs;
		}
		
		List<MessageCollection> messages = new ArrayList<MessageCollection>();
		for(MessageSource s : sources)
		{
			try
			{
				MessageCollection m = s.load(context, url);
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
	
	@Override
	public MessageCollection getMessages(Context context, Class<?> resource)
	{
		String key = resource.getName();
		MessageCollection msgs = context.getValue(key);
		if(msgs != null)
		{
			return msgs;
		}
		
		List<MessageCollection> messages = new ArrayList<MessageCollection>();
		for(MessageSource s : sources)
		{
			try
			{
				MessageCollection m = s.load(context, resource);
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
