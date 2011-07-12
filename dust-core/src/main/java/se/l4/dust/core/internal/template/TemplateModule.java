package se.l4.dust.core.internal.template;

import se.l4.crayon.CrayonModule;
import se.l4.crayon.annotation.Contribution;
import se.l4.dust.api.TemplateManager;
import se.l4.dust.api.annotation.TemplateScoped;
import se.l4.dust.api.template.TemplateCache;
import se.l4.dust.api.template.TemplateRenderer;
import se.l4.dust.api.template.TemplateScope;
import se.l4.dust.core.internal.TemplateManagerImpl;
import se.l4.dust.core.internal.template.components.BodyComponent;
import se.l4.dust.core.internal.template.components.HolderComponent;
import se.l4.dust.core.internal.template.components.IfComponent;
import se.l4.dust.core.internal.template.components.LoopComponent;
import se.l4.dust.core.internal.template.components.ParameterComponent;
import se.l4.dust.core.internal.template.components.RawComponent;
import se.l4.dust.core.internal.template.expression.MvelPropertySource;

/**
 * Module that activates template functions. Binds they default implementations
 * and add common components.
 * 
 * @author Andreas Holstenson
 *
 */
public class TemplateModule
	extends CrayonModule
{
	public static final String COMMON = "dust:common";
	
	@Override
	public void configure()
	{
		bind(TemplateManager.class).to(TemplateManagerImpl.class);
		
		bind(TemplateCache.class).to(TemplateCacheImpl.class);
		
		bind(TemplateRenderer.class).to(TemplateRendererImpl.class);
		
		bindScope(TemplateScoped.class, TemplateScope.INSTANCE);
	}
	
	@Contribution
	public void contributeCommonComponents(TemplateManager manager)
	{
		manager.getNamespace(COMMON)
			.addComponent(ParameterComponent.class, "parameter")
			.addComponent(BodyComponent.class, "body")
			.addComponent(IfComponent.class, "if")
			.addComponent(LoopComponent.class, "loop")
			.addComponent(HolderComponent.class, "holder")
			.addComponent(RawComponent.class, "raw");
	}
	
	@Contribution
	public void contributePropertySources(TemplateManager manager,
			CyclePropertySource s1,
			VarPropertySource s2,
			MvelPropertySource s3)
	{
		manager.addPropertySource("cycle", s1);
		manager.addPropertySource("var", s2);
		manager.addPropertySource("mvel", s3);
	}
}
