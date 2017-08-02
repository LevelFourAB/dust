package se.l4.dust.core.internal.template;

import com.google.inject.Stage;
import com.google.inject.name.Named;

import se.l4.crayon.Contribution;
import se.l4.crayon.Contributions;
import se.l4.crayon.CrayonModule;
import se.l4.crayon.Order;
import se.l4.dust.Dust;
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
import se.l4.dust.core.internal.template.components.RawComponent;
import se.l4.dust.core.internal.template.components.RenderComponent;
import se.l4.dust.core.internal.template.mixins.AttributesMixin;
import se.l4.dust.core.internal.template.mixins.IfMixin;
import se.l4.dust.core.internal.template.mixins.RepeatMixin;
import se.l4.dust.core.internal.template.mixins.SettersMixin;

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
			BodyComponent body,
			IfComponent ifC,
			LoopComponent loop,
			HolderComponent holder,
			RawComponent raw,
			RenderComponent render,
			IfMixin ifM,
			RepeatMixin repeatM,
			AttributesMixin attributesM)
	{
		manager.getNamespace(Dust.NAMESPACE_COMMON)
			.addFragment("body", body)
			.addFragment("if", ifC)
			.addFragment("loop", loop)
			.addFragment("holder", holder)
			.addFragment("raw", raw)
			.addFragment("render", render)
			.addMixin("if", ifM)
			.addMixin("repeat", repeatM)
			.addMixin("attributes", attributesM);
	}

	@TemplateContribution
	public void contributeFragmentComponents(Namespaces namespaces,
			Templates manager,
			FragmentDefinition definition,
			FragmentUse use)
	{
		namespaces.bind(Dust.NAMESPACE_FRAGMENTS).manual().add();
		manager.getNamespace(Dust.NAMESPACE_FRAGMENTS)
			.addFragment("define", definition)
			.addFragment("use", use);
	}

	@TemplateContribution
	public void contributeSetters(Namespaces namespaces,
			Templates manager,
			SettersMixin mixin)
	{
		namespaces.bind(Dust.NAMESPACE_SETTERS).manual().add();
		manager.getNamespace(Dust.NAMESPACE_SETTERS)
			.addMixin(mixin);
	}

	@Contribution
	@Named("dust-templates")
	@Order("after:dust-namespaces")
	public void bindNamespaces(@TemplateContribution Contributions contributions)
	{
		InternalContributions.add(contributions);

		contributions.run();
	}

	@Contribution
	@Named("dust-discovery-components")
	public void contributeComponentsDiscovery(NamespaceDiscovery discovery,
			ComponentDiscoveryHandler handler)
	{
		discovery.addHandler(handler);
	}

	@Contribution
	@Named("dust-discovery-template-loading")
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
