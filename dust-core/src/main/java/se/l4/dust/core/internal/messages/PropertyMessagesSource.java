package se.l4.dust.core.internal.messages;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Properties;

import se.l4.dust.api.Context;
import se.l4.dust.api.messages.AbstractMessages;
import se.l4.dust.api.messages.MessageSource;
import se.l4.dust.api.messages.Messages;
import se.l4.dust.api.resource.variant.ResourceVariant;
import se.l4.dust.api.resource.variant.ResourceVariantManager;

import com.google.common.io.Closeables;
import com.google.inject.Inject;

/**
 * Source of property based messages.
 * 
 * @author Andreas Holstenson
 *
 */
public class PropertyMessagesSource
	implements MessageSource
{
	private static final Charset ISO88591 = Charset.forName("ISO-8859-1");
	private static final Charset UTF8 = Charset.forName("UTF-8");
	
	private final ResourceVariantManager variants;

	@Inject
	public PropertyMessagesSource(ResourceVariantManager variants)
	{
		this.variants = variants;
	}
	

	private boolean checkIfUrlExists(String url)
	{
		InputStream stream = null;
		try
		{
			stream = new URL(url).openStream();
			return true;
		}
		catch(IOException e)
		{
			return false;
		}
		finally
		{
			Closeables.closeQuietly(stream);
		}
	}

	@Override
	public Messages load(Context context, String url)
		throws IOException
	{
		int idx = url.lastIndexOf('.');
		String firstPart = idx > 0 ? url.substring(0, idx) : url;
		
		url = firstPart + ".properties";
		ResourceVariantManager.Result result = variants.resolve(context, new ResourceVariantManager.ResourceCallback()
		{
			public boolean exists(ResourceVariant variant, String url)
				throws IOException
			{
				if(checkIfUrlExists(url))
				{
					return true;
				}
				else
				{
					return false;
				}
			}
		}, url);
		
		if(result.getUrl().equals(url) && ! checkIfUrlExists(url))
		{
			// No messages
			return null;
		}
		
		InputStream stream = new URL(result.getUrl()).openStream();
		try
		{
			Properties props = new Properties();
			props.load(stream);
			return new PropertyMessages(result.getVariant(), props);
		}
		finally
		{
			Closeables.closeQuietly(stream);
		}
	}

	private static class PropertyMessages
		extends AbstractMessages
	{
		private final Properties props;

		public PropertyMessages(ResourceVariant variant, Properties props)
		{
			super(variant);
			this.props = props;
		}
		
		@Override
		public String get(String property)
		{
			String v = props.getProperty(property);
			return v == null ? null : new String(v.getBytes(ISO88591), UTF8);
		}
	}
}
