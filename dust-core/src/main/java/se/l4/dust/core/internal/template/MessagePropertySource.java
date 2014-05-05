package se.l4.dust.core.internal.template;

import se.l4.dust.api.Context;
import se.l4.dust.api.TemplateException;
import se.l4.dust.api.messages.MessageManager;
import se.l4.dust.api.messages.Messages;
import se.l4.dust.api.resource.variant.ResourceVariant;
import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.api.template.dom.DynamicContent;
import se.l4.dust.api.template.dom.Text;
import se.l4.dust.api.template.dom.VariantContent;
import se.l4.dust.api.template.spi.PropertySource;
import se.l4.dust.api.template.spi.TemplateInfo;
import se.l4.dust.api.template.spi.TemplateVariant;

import com.google.inject.Inject;

public class MessagePropertySource
	implements PropertySource
{
	private static final String VARIANT_KEY = "__variant__";
	
	private final MessageManager messageManager;
	
	@Inject
	public MessagePropertySource(MessageManager messages)
	{
		this.messageManager = messages;
	}
	
	public DynamicContent getPropertyContent(TemplateInfo namespaces,
			Class<?> context, 
			String propertyExpression)
	{
		return new Content(namespaces.getURL(), propertyExpression);
	}
	
	private class Content
		extends DynamicContent
		implements VariantContent
	{
		private final String url;
		private final String property;

		public Content(String url, String property)
		{
			this.url = url;
			this.property = property;
		}
		
		@Override
		public Object getValue(RenderingContext ctx, Object root)
		{
			Messages messages = getMessages(ctx);
			String value = messages.get(property);
			if(value == null)
			{
				throw new TemplateException("The property " + property + " does not exist in the file " + url);
			}
			
			return value;
		}

		private Messages getMessages(Context ctx)
		{
			Messages messages;
			if(ctx.getValue(url) != null)
			{
				messages = ctx.getValue(url);
			}
			else
			{
				messages = messageManager.getMessages(ctx, url);
				ctx.putValue(url, messages);
			}
			
			return messages;
		}

		@Override
		public void setValue(RenderingContext ctx, Object root, Object data)
		{
			
		}
		
		public se.l4.dust.api.template.dom.Content doCopy()
		{
			return new Content(url, property);
		}

		public void transform(TemplateVariant variant)
		{
			Messages messages = getMessages(variant.getContext());
			Text text = new Text(messages.get(property));
			ResourceVariant rv = messages.getVariant();
			variant.replaceWith(text, rv);
		}
	}
}
