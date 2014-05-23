package se.l4.dust.core.internal.template;

import se.l4.crayon.Contributions;
import se.l4.crayon.CrayonModule;
import se.l4.crayon.annotation.Contribution;
import se.l4.crayon.annotation.Order;
import se.l4.dust.api.NamespaceManager;
import se.l4.dust.api.TemplateManager;
import se.l4.dust.api.annotation.TemplateContribution;
import se.l4.dust.api.discovery.NamespaceDiscovery;
import se.l4.dust.api.template.TemplateCache;
import se.l4.dust.api.template.TemplateRenderer;
import se.l4.dust.core.internal.InternalContributions;
import se.l4.dust.core.internal.TemplateManagerImpl;
import se.l4.dust.core.internal.template.components.BodyComponent;
import se.l4.dust.core.internal.template.components.FragmentDefinition;
import se.l4.dust.core.internal.template.components.FragmentUse;
import se.l4.dust.core.internal.template.components.HolderComponent;
import se.l4.dust.core.internal.template.components.IfComponent;
import se.l4.dust.core.internal.template.components.LoopComponent;
import se.l4.dust.core.internal.template.components.ParameterComponent;
import se.l4.dust.core.internal.template.components.RawComponent;
import se.l4.dust.core.internal.template.components.RenderComponent;

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
		
		bindContributions(TemplateContribution.class);
	}
	
	@TemplateContribution
	public void contributeCommonComponents(TemplateManager manager,
			ParameterComponent parameter,
			BodyComponent body,
			IfComponent ifC,
			LoopComponent loop,
			HolderComponent holder,
			RawComponent raw,
			RenderComponent render,
			IfMixin ifM)
	{
		manager.getNamespace(COMMON)
			.addFragment("parameter", parameter)
			.addFragment("body", body)
			.addFragment("if", ifC)
			.addFragment("loop", loop)
			.addFragment("holder", holder)
			.addFragment("raw", raw)
			.addFragment("render", render)
			.addMixin("if", ifM);
	}
	
	@TemplateContribution
	public void contributeFragmentComponents(NamespaceManager namespaces,
			TemplateManager manager,
			FragmentDefinition definition,
			FragmentUse use)
	{
		namespaces.bind("dust:fragments").add();
		manager.getNamespace("dust:fragments")
			.addFragment("define", definition)
			.addFragment("use", use);
	}
	
	@Contribution(name="dust-templates")
	@Order("after:dust-namespaces")
	public void bindNamespaces(@TemplateContribution Contributions contributions)
	{
		InternalContributions.add(contributions);
		
		contributions.run();
	}
	
	@Contribution(name="dust-discovery-components")
	public void contributeComponentsDiscovery(NamespaceDiscovery discovery,
			ComponentDiscoveryHandler handler)
	{
		discovery.addHandler(handler);
	}
	
	@Contribution(name="dust-discovery-template-loading")
	@Order("after:dust-discovery-components")
	public void contributePreloadingDiscovery(NamespaceDiscovery discovery,
			TemplatePreloadingHandler handler)
	{
		discovery.addHandler(handler);
	}
}
