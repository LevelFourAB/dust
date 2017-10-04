package se.l4.dust.core.internal.messages;

import java.text.MessageFormat;
import java.util.Locale;

import com.google.inject.Inject;
import com.google.inject.Stage;

import se.l4.dust.api.Context;
import se.l4.dust.api.expression.AbstractDynamicProperty;
import se.l4.dust.api.expression.DynamicMethod;
import se.l4.dust.api.expression.DynamicProperty;
import se.l4.dust.api.expression.ExpressionEncounter;
import se.l4.dust.api.expression.ExpressionException;
import se.l4.dust.api.expression.ExpressionSource;
import se.l4.dust.api.messages.MessageCollection;
import se.l4.dust.api.messages.Messages;
import se.l4.dust.api.resource.ResourceLocation;
import se.l4.dust.api.resource.variant.ResourceVariant;

public class MessageExpressionSource
	implements ExpressionSource
{
	private final Messages messageManager;
	private final boolean debug;

	@Inject
	public MessageExpressionSource(Stage stage, Messages messageManager)
	{
		this.messageManager = messageManager;
		debug = stage == Stage.DEVELOPMENT;
	}

	protected ResourceLocation getLocation(ExpressionEncounter encounter)
	{
		return encounter.getSource();
	}

	@Override
	public DynamicProperty getProperty(ExpressionEncounter encounter, String name)
	{
		ResourceLocation location = getLocation(encounter);
		return debug
			? new DebuggingMessageProperty(messageManager, location, name)
			: new MessageProperty(messageManager, location, name);
	}

	@Override
	public DynamicMethod getMethod(ExpressionEncounter encounter, String name, Class... parameters)
	{
		if("get".equals(name) && encounter.isRoot() && parameters.length == 1)
		{
			return new MessageGetMethod(messageManager, getLocation(encounter));
		}

		return null;
	}

	private static MessageCollection getMessages(Messages manager, Context context, ResourceLocation location)
	{
		return manager.getMessages(context, location);
	}

	private static class MessageProperty
		extends AbstractDynamicProperty
	{
		protected final Messages manager;
		protected final String propertyName;
		protected final ResourceLocation resource;

		public MessageProperty(Messages manager, ResourceLocation resource, String propertyName)
		{
			this.manager = manager;
			this.resource = resource;
			this.propertyName = propertyName;
		}

		@Override
		public Class<?> getType()
		{
			return String.class;
		}

		@Override
		public Object get(Context context, Object root)
		{
			MessageCollection messages = getMessages(manager, context, resource);
			return messages.get(propertyName);
		}

		@Override
		public boolean supportsGet()
		{
			return true;
		}

		@Override
		public void set(Context context, Object root, Object value)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean supportsSet()
		{
			return false;
		}

		@Override
		public boolean needsContext()
		{
			return false;
		}

		@Override
		public DynamicProperty getProperty(ExpressionEncounter encounter, String name)
		{
			return new MessageProperty(manager, resource, propertyName + "." + name);
		}

		@Override
		public DynamicMethod getMethod(ExpressionEncounter encounter, String name, Class... parameters)
		{
			if("format".equals(name))
			{
				return new MessageFormatMethod(manager, resource, propertyName, parameters);
			}
			else if("get".equals(name) && parameters.length == 1)
			{
				return new MessageGetLocalMethod(manager, resource, propertyName);
			}
			else if("asCollection".equals(name) && parameters.length == 0)
			{
				return new MessageCollectionMethod(manager, resource, propertyName);
			}
			return super.getMethod(encounter, name, parameters);
		}
	}

	private static class DebuggingMessageProperty
		extends MessageProperty
	{
		public DebuggingMessageProperty(Messages manager, ResourceLocation resource, String propertyName)
		{
			super(manager, resource, propertyName);
		}

		@Override
		public Object get(Context context, Object root)
		{
			Object result = super.get(context, root);
			if(result == null)
			{
				throw new ExpressionException("No text for `" + propertyName + "` could be found");
			}

			return result;
		}

		@Override
		public DynamicProperty getProperty(ExpressionEncounter encounter, String name)
		{
			return new DebuggingMessageProperty(manager, resource, propertyName + "." + name);
		}
	}

	private static class MessageFormatMethod
		implements DynamicMethod
	{
		private final Messages manager;
		private final ResourceLocation resource;
		private final String propertyName;
		private final Class[] parameters;

		public MessageFormatMethod(Messages manager,
				ResourceLocation resource,
				String propertyName,
				Class[] parameters)
		{
			this.manager = manager;
			this.resource = resource;
			this.propertyName = propertyName;
			this.parameters = parameters;
		}

		@Override
		public Object invoke(Context context, Object instance, Object... parameters)
		{
			MessageCollection messages = getMessages(manager, context, resource);
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
		public Class<?>[] getParametersType()
		{
			return parameters;
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
		private static final Class<?>[] SINGLE_STRING_PARAMETER = new Class[] { String.class };

		private final Messages manager;
		private final ResourceLocation resource;

		public MessageGetMethod(Messages manager, ResourceLocation resource)
		{
			this.manager = manager;
			this.resource = resource;
		}

		@Override
		public Object invoke(Context context, Object instance, Object... parameters)
		{
			MessageCollection messages = getMessages(manager, context, resource);
			return messages.get(parameters[0].toString());
		}

		@Override
		public Class<?> getType()
		{
			return String.class;
		}

		@Override
		public Class<?>[] getParametersType()
		{
			return SINGLE_STRING_PARAMETER;
		}

		@Override
		public boolean needsContext()
		{
			return false;
		}
	}

	private static class MessageGetLocalMethod
		implements DynamicMethod
	{
		private static final Class<?>[] SINGLE_STRING_PARAMETER = new Class[] { String.class };

		private final Messages manager;
		private final ResourceLocation resource;
		private final String propertyName;

		public MessageGetLocalMethod(Messages manager, ResourceLocation resource, String propertyName)
		{
			this.manager = manager;
			this.resource = resource;
			this.propertyName = propertyName;
		}

		@Override
		public Object invoke(Context context, Object instance, Object... parameters)
		{
			MessageCollection messages = getMessages(manager, context, resource);
			return messages.get(propertyName + '.' + parameters[0].toString());
		}

		@Override
		public Class<?> getType()
		{
			return String.class;
		}

		@Override
		public Class<?>[] getParametersType()
		{
			return SINGLE_STRING_PARAMETER;
		}

		@Override
		public boolean needsContext()
		{
			return false;
		}
	}

	private static class MessageCollectionMethod
		implements DynamicMethod
	{
		private static final Class<?>[] SINGLE_STRING_PARAMETER = new Class[0];

		private final Messages manager;
		private final ResourceLocation resource;
		private final String propertyName;

		public MessageCollectionMethod(Messages manager, ResourceLocation resource, String propertyName)
		{
			this.manager = manager;
			this.resource = resource;
			this.propertyName = propertyName;
		}

		@Override
		public Object invoke(Context context, Object instance, Object... parameters)
		{
			MessageCollection messages = getMessages(manager, context, resource);
			return new ScopedMessageCollection(messages, propertyName);
		}

		@Override
		public Class<?> getType()
		{
			return MessageCollection.class;
		}

		@Override
		public Class<?>[] getParametersType()
		{
			return SINGLE_STRING_PARAMETER;
		}

		@Override
		public boolean needsContext()
		{
			return false;
		}
	}
}
