package se.l4.dust.core.internal.messages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import se.l4.dust.api.Context;
import se.l4.dust.api.TemplateException;
import se.l4.dust.api.messages.MessageManager;
import se.l4.dust.api.messages.MessageSource;
import se.l4.dust.api.messages.Messages;

import com.google.inject.Singleton;

/**
 * Implementation of {@link MessageManager}.
 * 
 * @author Andreas Holstenson
 *
 */
@Singleton
public class MessageMangerImpl
	implements MessageManager
{
	private final List<MessageSource> sources;
	
	public MessageMangerImpl()
	{
		sources = new CopyOnWriteArrayList<MessageSource>();
	}
	
	@Override
	public void addSource(MessageSource source)
	{
		sources.add(source);
	}

	@Override
	public Messages getMessages(Context context, String url)
	{
		List<Messages> messages = new ArrayList<Messages>();
		for(MessageSource s : sources)
		{
			try
			{
				Messages msgs = s.load(context, url);
				if(msgs != null)
				{
					messages.add(msgs);
				}
			}
			catch(IOException e)
			{
				throw new TemplateException("Unable to load messages; " + e.getMessage(), e);
			}
		}
		
		if(messages.size() == 1)
		{
			return messages.get(0);
		}
		else
		{
			return new DelegatingMessages(messages);
		}
	}


}
