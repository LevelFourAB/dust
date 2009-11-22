package se.l4.dust.core.internal;

import javax.servlet.ServletContext;

import com.google.inject.Binder;

import se.l4.crayon.annotation.Description;

public class ServletContextModule
{
	private final ServletContext ctx;
	
	public ServletContextModule(ServletContext ctx)
	{
		this.ctx = ctx;
	}
	
	@Description
	public void describe(Binder binder)
	{
		// Bind the context
		binder.bind(ServletContext.class).toInstance(ctx);
	}
}
