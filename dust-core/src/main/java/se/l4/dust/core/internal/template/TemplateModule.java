package se.l4.dust.core.internal.template;

import org.jdom.JDOMException;
import org.jdom.Namespace;

import com.google.inject.Injector;

import se.l4.crayon.CrayonModule;
import se.l4.crayon.annotation.Contribution;
import se.l4.dust.api.DocumentLinker;
import se.l4.dust.api.TemplateFilter;
import se.l4.dust.api.TemplateManager;
import se.l4.dust.api.template.TemplateCache;
import se.l4.dust.api.template.TemplateRenderer;
import se.l4.dust.core.internal.DocumentLinkerImpl;
import se.l4.dust.core.internal.TemplateManagerImpl;
import se.l4.dust.core.internal.template.components.BodyComponent;
import se.l4.dust.core.internal.template.components.HolderComponent;
import se.l4.dust.core.internal.template.components.IfComponent;
import se.l4.dust.core.internal.template.components.LinkComponent;
import se.l4.dust.core.internal.template.components.LoopComponent;
import se.l4.dust.core.internal.template.components.ParameterComponent;
import se.l4.dust.core.internal.template.components.RawComponent;
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
	}
	
	@Contribution
	public void contributeCommonComponents(TemplateManagerImpl manager)
	{
		manager.addComponent(ParameterComponent.class);
		manager.addComponent(BodyComponent.class);
		
		manager.addComponent(IfComponent.class);
		manager.addComponent(LoopComponent.class);
		manager.addComponent(LinkComponent.class);
		manager.addComponent(HolderComponent.class);
		manager.addComponent(RawComponent.class);
	}
	
	@Contribution
	public void contributePropertySources(TemplateManager manager)
	{
		manager.addPropertySource("cycle", CyclePropertySource.class);
		manager.addPropertySource("var", VarPropertySource.class);
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
