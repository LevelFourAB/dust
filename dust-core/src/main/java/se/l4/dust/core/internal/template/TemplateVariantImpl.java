package se.l4.dust.core.internal.template;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import se.l4.dust.api.Context;
import se.l4.dust.api.TemplateException;
import se.l4.dust.api.resource.variant.ResourceVariant;
import se.l4.dust.api.resource.variant.ResourceVariantManager;
import se.l4.dust.api.resource.variant.ResourceVariantManager.ResourceCallback;
import se.l4.dust.api.template.dom.Comment;
import se.l4.dust.api.template.dom.Content;
import se.l4.dust.api.template.dom.Element;
import se.l4.dust.api.template.dom.Element.Attribute;
import se.l4.dust.api.template.dom.ParsedTemplate;
import se.l4.dust.api.template.dom.Text;
import se.l4.dust.api.template.dom.VariantContent;
import se.l4.dust.api.template.spi.TemplateVariant;
import se.l4.dust.core.internal.resource.MergedResourceVariant;

/**
 * Variant transformer for templates.
 * 
 * @author Andreas Holstenson
 *
 */
public class TemplateVariantImpl
	implements TemplateVariant
{
	private final ResourceVariantManager variants;
	private final Context context;
	private final ParsedTemplate template;
	private final String url;
	
	private HashSet<ResourceVariant> found;
	private Content replaced;
	private String transformedUrl;
	private ParsedTemplate transformed;

	public TemplateVariantImpl(ResourceVariantManager variants, Context context, ParsedTemplate template, String url)
	{
		this.variants = variants;
		this.context = context;
		this.template = template;
		this.url = url;
	}

	public Context getContext()
	{
		return context;
	}

	public ResourceVariantManager getVariantManager()
	{
		return variants;
	}

	public ParsedTemplate getTransformedTemplate()
	{
		return transformed;
	}
	
	public String getTransformedUrl()
	{
		return transformedUrl;
	}
	
	public void transform()
	{
		found = new HashSet<ResourceVariant>();
		
		Content root = transform(template.getRoot());
		
		if(found.isEmpty())
		{
			transformed = new ParsedTemplate(template.getUrl(), template.getName(), template.getDocType(), (Element) root, template.getRawId());
			transformedUrl = url;
			return;
		}
		
		try
		{
			ResourceVariantManager.Result result = variants.resolveNoCache(context, new ResourceCallback()
			{
				public boolean exists(ResourceVariant variant, String url)
					throws IOException
				{
					for(ResourceVariant v : found)
					{
						if(((MergedResourceVariant) variant).hasSpecific(v))
						{
							return true;
						}
					}
					
					return false;
				}
			}, url);
			
			transformed = new ParsedTemplate(template.getUrl(), template.getName(), template.getDocType(), (Element) root, template.getRawId());
			transformedUrl = result.getUrl();
		}
		catch(IOException e)
		{
			throw new TemplateException("Unable to transform the template at " + url + "; " + e.getMessage(), e);
		}
	}
	
	private Content transform(Content content)
	{
		if(content instanceof Comment)
		{
			Comment c = (Comment) content.copy();
			List<Content> children = new ArrayList<Content>();
			for(Content child : ((Comment) content).getRawContents())
			{
				children.add(transform(child));
			}
			c.addContent(children);
			return c;
		}
		else if(content instanceof Element)
		{
			Element c = (Element) content.copy();
			for(Attribute a : c.getAttributes())
			{
				Content[] values = a.getValue();
				Content[] copy = new Content[values.length];
				for(int i=0, n=values.length; i<n; i++)
				{
					copy[i] = transform(values[i]);
				}
				
				c.setAttribute(a.getName(), copy);
			}
			List<Content> children = new ArrayList<Content>();
			for(Content child : ((Element) content).getRawContents())
			{
				children.add(transform(child));
			}
			c.addContent(children);
			return c;
		}
		else if(content instanceof VariantContent)
		{
			replaced = null;
			((VariantContent) content).transform(this);
			if(replaced != null)
			{
				return replaced;
			}
			else
			{
				return content.copy();
			}
		}
		else if(content instanceof Text)
		{
			return content.copy();
		}
		else
		{
			return content.copy();
		}
	}

	public void replaceWith(Content content, ResourceVariant variant)
	{
		if(variant != null)
		{
			found.add(variant);
		}
		
		replaced = content;
	}

}
