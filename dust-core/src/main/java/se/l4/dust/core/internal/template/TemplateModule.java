package se.l4.dust.core.internal.template;

import se.l4.crayon.Contributions;
import se.l4.crayon.CrayonModule;
import se.l4.crayon.annotation.Contribution;
import se.l4.crayon.annotation.Order;
import se.l4.dust.api.Namespaces;
import se.l4.dust.api.discovery.NamespaceDiscovery;
import se.l4.dust.api.template.TemplateCache;
import se.l4.dust.api.template.TemplateContribution;
import se.l4.dust.api.template.TemplateRenderer;
import se.l4.dust.api.template.Templates;
import se.l4.dust.core.internal.InternalContributions;
import se.l4.dust.core.internal.template.components.BodyComponent;
import se.l4.dust.core.internal.template.components.FragmentDefinition;
import se.l4.dust.core.internal.template.components.FragmentUse;
import se.l4.dust.core.internal.template.components.HolderComponent;
import se.l4.dust.core.internal.template.components.IfComponent;
import se.l4.dust.core.internal.template.components.LoopComponent;
import se.l4.dust.core.internal.template.components.ParameterComponent;
import se.l4.dust.core.internal.template.components.RawComponent;
import se.l4.dust.core.internal.template.components.RenderComponent;
import se.l4.dust.core.internal.template.mixins.IfMixin;
import se.l4.dust.core.internal.template.mixins.RepeatMixin;

import com.google.inject.Stage;

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
		bind(Templates.class).to(TemplatesImpl.class);
		
		bind(TemplateCache.class).to(TemplateCacheImpl.class);
		
		bind(TemplateRenderer.class).to(TemplateRendererImpl.class);
		
		bindContributions(TemplateContribution.class);
	}
	
	@TemplateContribution
	public void contributeCommonComponents(Templates manager,
			ParameterComponent parameter,
			BodyComponent body,
			IfComponent ifC,
			LoopComponent loop,
			HolderComponent holder,
			RawComponent raw,
			RenderComponent render,
			IfMixin ifM,
			RepeatMixin repeatM)
	{
		manager.getNamespace(COMMON)
			.addFragment("parameter", parameter)
			.addFragment("body", body)
			.addFragment("if", ifC)
			.addFragment("loop", loop)
			.addFragment("holder", holder)
			.addFragment("raw", raw)
			.addFragment("render", render)
			.addMixin("if", ifM)
			.addMixin("repeat", repeatM);
	}
	
	@TemplateContribution
	public void contributeFragmentComponents(Namespaces namespaces,
			Templates manager,
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
			Stage stage,
			TemplatePreloadingHandler handler)
	{
		if(stage != Stage.DEVELOPMENT)
		{
			discovery.addHandler(handler);
		}
	}
}
