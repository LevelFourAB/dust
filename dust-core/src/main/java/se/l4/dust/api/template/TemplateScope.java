package se.l4.dust.api.template;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.google.inject.Scope;

import se.l4.dust.core.internal.template.TemplateContext;

public class TemplateScope
	implements Scope
{
	public static final TemplateScope INSTANCE = new TemplateScope();
	
	public <T> Provider<T> scope(final Key<T> key, final Provider<T> provider)
	{
		return new Provider<T>()
		{
			@SuppressWarnings("unchecked")
			public T get()
			{
				RenderingContext ctx = TemplateContext.get();
				if(ctx == null)
				{
					throw new ProvisionException("Template scope is only available during template rendering");
				}
				
				Object o = ctx.getValue(key);
				if(o == null)
				{
					o = provider.get();
					ctx.putValue(key, o);
				}
					
				return (T) o;
			}
			
			@Override
			public String toString()
			{
				return "TemplateScope";
			}
		};
	}
}
