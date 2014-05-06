package se.l4.dust.core.internal.messages;

import java.text.MessageFormat;
import java.util.Locale;

import se.l4.dust.api.Context;
import se.l4.dust.api.expression.AbstractDynamicProperty;
import se.l4.dust.api.expression.DynamicMethod;
import se.l4.dust.api.expression.DynamicProperty;
import se.l4.dust.api.expression.ExpressionEncounter;
import se.l4.dust.api.expression.ExpressionException;
import se.l4.dust.api.expression.ExpressionSource;
import se.l4.dust.api.messages.MessageManager;
import se.l4.dust.api.messages.Messages;
import se.l4.dust.api.resource.variant.ResourceVariant;

import com.google.inject.Inject;
import com.google.inject.Stage;

public class MessageExpressionSource
	implements ExpressionSource
{
	private final MessageManager messageManager;
	private final boolean debug;

	@Inject
	public MessageExpressionSource(Stage stage, MessageManager messageManager)
	{
		this.messageManager = messageManager;
		debug = stage == Stage.DEVELOPMENT;
	}
	
	protected String getUrl(ExpressionEncounter encounter)
	{
		return encounter.getSource().toString().intern();
	}

	@Override
	public DynamicProperty getProperty(ExpressionEncounter encounter, String name)
	{
		String url = getUrl(encounter);
		return debug
			? new DebuggingMessageProperty(messageManager, url, name)
			: new MessageProperty(messageManager, url, name);
	}

	@Override
	public DynamicMethod getMethod(ExpressionEncounter encounter, String name, Class... parameters)
	{
		String url = getUrl(encounter);
		if("get".equals(name) && encounter.isRoot())
		{
			return new MessageGetMethod(messageManager, url);
		}
		
		return null;
	}
	
	private static Messages getMessages(MessageManager manager, Context context, String url)
	{
		return manager.getMessages(context, url);
	}
	
	private static class MessageProperty
		extends AbstractDynamicProperty
	{
		protected final MessageManager manager;
		protected final String url;
		protected final String propertyName;

		public MessageProperty(MessageManager manager, String url, String propertyName)
		{
			this.manager = manager;
			this.url = url;
			this.propertyName = propertyName;
		}
		
		@Override
		public Class<?> getType()
		{
			return String.class;
		}
		
		@Override
		public Object getValue(Context context, Object root)
		{
			Messages messages = getMessages(manager, context, url);
			return messages.get(propertyName);
		}
		
		@Override
		public void setValue(Context context, Object root, Object value)
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		public boolean needsContext()
		{
			return false;
		}
		
		@Override
		public DynamicProperty getProperty(ExpressionEncounter encounter, String name)
		{
			return new MessageProperty(manager, url, propertyName + "." + name);
		}
		
		@Override
		public DynamicMethod getMethod(ExpressionEncounter encounter, String name, Class... parameters)
		{
			String url = encounter.getSource().toString().intern();
			
			if("format".equals(name))
			{
				return new MessageFormatMethod(manager, url, propertyName);
			}
			
			return super.getMethod(encounter, name, parameters);
		}
	}
	
	private static class DebuggingMessageProperty
		extends MessageProperty
	{
		public DebuggingMessageProperty(MessageManager manager, String url, String propertyName)
		{
			super(manager, url, propertyName);
		}
		
		@Override
		public Object getValue(Context context, Object root)
		{
			Object result = super.getValue(context, root);
			if(result == null)
			{
				throw new ExpressionException("No message for `" + propertyName + "` could not be found");
			}
			
			return result;
		}
	}
	
	private static class MessageFormatMethod
		implements DynamicMethod
	{
		private final MessageManager manager;
		private final String url;
		private final String propertyName;

		public MessageFormatMethod(MessageManager manager, String url, String propertyName)
		{
			this.manager = manager;
			this.url = url;
			this.propertyName = propertyName;
		}
		
		@Override
		public Object invoke(Context context, Object instance, Object... parameters)
		{
			Messages messages = getMessages(manager, context, url);
			String message = messages.get(propertyName);
			MessageFormat format = context.getValue(message);
			if(format == null)
			{
				Locale locale = context.getValue(ResourceVariant.LOCALE);
				format = new MessageFormat(message, locale);
				context.putValue(message, format);
			}
			
			return format.format(parameters);
		}

		@Override
		public Class<?> getType()
		{
			return String.class;
		}
	
		@Override
		public boolean needsContext()
		{
			return false;
		}
	}
	
	private static class MessageGetMethod
		implements DynamicMethod
	{
		private final MessageManager manager;
		private final String url;
	
		public MessageGetMethod(MessageManager manager, String url)
		{
			this.manager = manager;
			this.url = url;
		}
		
		@Override
		public Object invoke(Context context, Object instance, Object... parameters)
		{
			Messages messages = getMessages(manager, context, url);
			return messages.get(parameters[0].toString());
		}
	
		@Override
		public Class<?> getType()
		{
			return String.class;
		}
	
		@Override
		public boolean needsContext()
		{
			return false;
		}
	}
}
