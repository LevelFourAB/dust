package se.l4.dust.core;

import com.google.inject.Binder;

import se.l4.crayon.annotation.Description;
import se.l4.dust.api.PageManager;
import se.l4.dust.core.internal.NormalPageManager;

public class NormalWebModule
	extends WebModule
{
	@Description
	public void describe(Binder binder)
	{
		binder.bind(PageManager.class).to(NormalPageManager.class);
	}
}
