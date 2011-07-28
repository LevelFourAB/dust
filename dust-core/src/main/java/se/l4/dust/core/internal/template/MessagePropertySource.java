package se.l4.dust.core.internal.template;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import com.google.inject.Inject;

import se.l4.dust.api.Context;
import se.l4.dust.api.TemplateException;
import se.l4.dust.api.resource.variant.ResourceVariant;
import se.l4.dust.api.resource.variant.ResourceVariantManager;
import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.api.template.dom.DynamicContent;
import se.l4.dust.api.template.dom.Text;
import se.l4.dust.api.template.dom.VariantContent;
import se.l4.dust.api.template.spi.PropertySource;
import se.l4.dust.api.template.spi.TemplateInfo;
import se.l4.dust.api.template.spi.TemplateVariant;

public class MessagePropertySource
	implements PropertySource
{
	private static final String VARIANT_KEY = "__variant__";
	
	private static final Charset ISO88591 = Charset.forName("ISO-8859-1");
	private static final Charset UTF8 = Charset.forName("UTF-8");
	
	private final ResourceVariantManager variants;
	
	@Inject
	public MessagePropertySource(ResourceVariantManager variants)
	{
		this.variants = variants;
	}
	
	public DynamicContent getPropertyContent(TemplateInfo namespaces,
			Class<?> context, 
			String propertyExpression)
	{
		return new Content(namespaces.getURL(), propertyExpression);
	}
	
	private Properties load(Context ctx, String url)
		throws IOException
	{
		int idx = url.lastIndexOf('.');
		String firstPart = idx > 0 ? url.substring(0, idx) : url;
		
		final AtomicReference<ResourceVariant> v = new AtomicReference<ResourceVariant>();
		
		url = firstPart + ".properties";
		url = variants.resolve(ctx, new ResourceVariantManager.ResourceCallback()
		{
			public boolean exists(ResourceVariant variant, String url)
				throws IOException
			{
				try
				{
					InputStream stream = new URL(url).openStream();
					stream.close();
					v.set(variant);
					return true;
				}
				catch(IOException e)
				{
					return false;
				}
			}
		}, url);
		
		Properties props = new Properties();
		InputStream stream = new URL(url).openStream();
		props.load(stream);
		
		if(v.get() != null)
		{
			props.put(VARIANT_KEY, v.get());
		}
		
		stream.close();
		
		return props;
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
		
		private String getValue(Properties props)
		{
			String value = props.getProperty(property);
			return new String(value.getBytes(ISO88591), UTF8);
		}
		
		@Override
		public Object getValue(RenderingContext ctx, Object root)
		{
			Properties props = getProperties(ctx);
			
			return getValue(props);
		}

		private Properties getProperties(Context ctx)
		{
			Properties props;
			if(ctx.getValue(url) != null)
			{
				props = ctx.getValue(url);
			}
			else
			{
				try
				{
					props = load(ctx, url);
				}
				catch(IOException e)
				{
					throw new TemplateException("Unable to load " + url);
				}
				ctx.putValue(url, props);
			}
			return props;
		}

		@Override
		public void setValue(RenderingContext ctx, Object root, Object data)
		{
			
		}
		
		public se.l4.dust.api.template.dom.Content copy()
		{
			return new Content(url, property);
		}

		public void transform(TemplateVariant variant)
		{
			Properties props = getProperties(variant.getContext());
			Text text = new Text(getValue(props));
			ResourceVariant rv = (ResourceVariant) props.get(VARIANT_KEY);
			variant.replaceWith(text, rv);
		}
	}
}
