package se.l4.dust.core.internal.template;

import org.jdom.JDOMException;
import org.jdom.Namespace;

import com.google.inject.Injector;

import se.l4.crayon.CrayonModule;
import se.l4.crayon.annotation.Contribution;
import se.l4.dust.api.DocumentLinker;
import se.l4.dust.api.TemplateFilter;
import se.l4.dust.api.TemplateManager;
import se.l4.dust.api.annotation.TemplateScoped;
import se.l4.dust.api.template.TemplateCache;
import se.l4.dust.api.template.TemplateRenderer;
import se.l4.dust.api.template.TemplateScope;
import se.l4.dust.core.internal.DocumentLinkerImpl;
import se.l4.dust.core.internal.TemplateManagerImpl;
import se.l4.dust.core.internal.template.components.BodyComponent;
import se.l4.dust.core.internal.template.components.HolderComponent;
import se.l4.dust.core.internal.template.components.IfComponent;
import se.l4.dust.core.internal.template.components.LoopComponent;
import se.l4.dust.core.internal.template.components.ParameterComponent;
import se.l4.dust.core.internal.template.components.RawComponent;
import se.l4.dust.core.internal.template.expression.MvelPropertySource;
import se.l4.dust.dom.Document;
import se.l4.dust.dom.Element;

public class TemplateModule
	extends CrayonModule
{
	public static final Namespace COMMON = Namespace.getNamespace("dust:common");
	
	@Override
	public void configure()
	{
		bind(TemplateManager.class).to(TemplateManagerImpl.class);
		
		bind(TemplateCache.class).to(TemplateCacheImpl.class);
		
		bind(TemplateRenderer.class).to(TemplateRendererImpl.class);
		
		bind(DocumentLinker.class).to(DocumentLinkerImpl.class);
		
		bindScope(TemplateScoped.class, TemplateScope.INSTANCE);
	}
	
	@Contribution
	public void contributeCommonComponents(TemplateManagerImpl manager)
	{
		manager.addComponent(COMMON, ParameterComponent.class, "parameter");
		manager.addComponent(COMMON, BodyComponent.class, "body");
		
		manager.addComponent(COMMON, IfComponent.class, "if");
		manager.addComponent(COMMON, LoopComponent.class, "loop");
		manager.addComponent(COMMON, HolderComponent.class, "holder");
		manager.addComponent(COMMON, RawComponent.class, "raw");
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
	
	@Contribution(name="document-linker")
	public void contributeDocumentLinkerFilter(TemplateManager registry,
			final Injector injector)
	{
		registry.addFilter(new TemplateFilter()
		{
			public void filter(Document document)
				throws JDOMException
			{
				Element root = document.getRootElement();
				if(false == root.getName().equals("html"))
				{
					return;
				}
				
				root = root.getChild("head");
				if(root == null)
				{
					return;
				}
				
				DocumentLinkerImpl linker 
					= injector.getInstance(DocumentLinkerImpl.class);
				
				root.addContent(linker.getElements());
			}
		});
	}
}
