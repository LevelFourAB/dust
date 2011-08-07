package se.l4.dust.js.templates.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import se.l4.dust.api.annotation.Component;
import se.l4.dust.api.annotation.Template;
import se.l4.dust.api.asset.AssetSource;
import se.l4.dust.api.resource.MemoryResource;
import se.l4.dust.api.resource.Resource;
import se.l4.dust.api.template.RenderingContext;
import se.l4.dust.api.template.TemplateCache;
import se.l4.dust.api.template.dom.ParsedTemplate;
import se.l4.dust.js.templates.JavaScriptTemplates;

import com.google.common.base.Charsets;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class JavaScriptTemplatesImpl
	implements JavaScriptTemplates, AssetSource
{
	private final TemplateCache templates;
	private final Provider<RenderingContext> contexts;
	private final Map<Key, BuilderImpl> builders;

	@Inject
	public JavaScriptTemplatesImpl(TemplateCache templates,
			Provider<RenderingContext> contexts)
	{
		this.templates = templates;
		this.contexts = contexts;
		builders = new HashMap<Key, BuilderImpl>();
	}
	
	public Builder addAsset(String namespace, String pathToFile)
	{
		BuilderImpl impl = new BuilderImpl();
		builders.put(new Key(namespace, pathToFile), impl);
		return impl;
	}

	public Resource locate(String ns, String pathToFile) throws IOException
	{
		BuilderImpl builder = builders.get(new Key(ns, pathToFile));
		if(builder == null) return null;
		
		String content = builder.getStringContents();
		return new MemoryResource("text/javascript", "UTF-8", 
			new ByteArrayInputStream(content.getBytes(Charsets.UTF_8))
		);
	}
	
	private class BuilderImpl
		implements Builder
	{
		private final Map<String, Class<?>> components;
		
		public BuilderImpl()
		{
			components = new HashMap<String, Class<?>>();
		}
		
		public Builder addComponent(Class<?> component)
		{
			return addComponent(getName(component), component);
		}
		
		public Builder addComponent(String name, Class<?> component)
		{
			components.put(name, component);
			return this;
		}
		
		public String getStringContents()
			throws IOException
		{
			StringBuilder result = new StringBuilder();
			result.append("var dust = dust || {};\n");
			result.append("dust.templates = dust.templates || {};\n");
			
			RenderingContext ctx = contexts.get();
			
			for(Map.Entry<String, Class<?>> e : components.entrySet())
			{
				ParsedTemplate tpl = templates.getTemplate(
					ctx, 
					e.getValue(), 
					e.getValue().getAnnotation(Template.class)
				);
				
				String function = new TemplateConverter(tpl, ctx).transform();
				
				result.append("dust.templates.")
					.append(e.getKey())
					.append("=")
					.append(function)
					.append(";");
			}
			
			return result.toString();
		}
		
		public String getName(Class<?> component)
		{
			Component annotation = component.getAnnotation(Component.class);
			if(annotation != null)
			{
				String[] names = annotation.value();
				if(names.length > 0)
				{
					return names[0];
				}
			}
			
			return component.getSimpleName().toLowerCase();
		}
	}
	
	private static class Key
	{
		private final String namespace;
		private final String path;

		public Key(String namespace, String path)
		{
			this.namespace = namespace;
			this.path = path;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((namespace == null) ? 0 : namespace.hashCode());
			result = prime * result + ((path == null) ? 0 : path.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if(this == obj)
				return true;
			if(obj == null)
				return false;
			if(getClass() != obj.getClass())
				return false;
			Key other = (Key) obj;
			if(namespace == null)
			{
				if(other.namespace != null)
					return false;
			}
			else if(!namespace.equals(other.namespace))
				return false;
			if(path == null)
			{
				if(other.path != null)
					return false;
			}
			else if(!path.equals(other.path))
				return false;
			return true;
		}
	}
}
