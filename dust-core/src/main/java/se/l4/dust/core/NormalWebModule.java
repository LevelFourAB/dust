package se.l4.dust.core;

import se.l4.crayon.annotation.Dependencies;
import se.l4.crayon.annotation.Description;
import se.l4.dust.api.PageManager;
import se.l4.dust.core.internal.DiscoveryModule;
import se.l4.dust.core.internal.NormalPageManager;

import com.google.inject.Binder;



@Dependencies({ WebModule.class, DiscoveryModule.class })
public class NormalWebModule
{
	@Description
	public void describe(Binder binder)
	{
		binder.bind(PageManager.class).to(NormalPageManager.class);
	}
}
