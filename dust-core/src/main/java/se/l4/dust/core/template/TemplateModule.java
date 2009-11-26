package se.l4.dust.core.template;

import org.jdom.JDOMException;
import org.jdom.Namespace;

import com.google.inject.Binder;
import com.google.inject.Injector;

import se.l4.crayon.annotation.Contribution;
import se.l4.crayon.annotation.Description;
import se.l4.dust.api.DocumentLinker;
import se.l4.dust.api.TemplateFilter;
import se.l4.dust.api.TemplateManager;
import se.l4.dust.core.internal.DocumentLinkerImpl;
import se.l4.dust.core.internal.TemplateManagerImpl;
import se.l4.dust.core.internal.template.TemplateCacheImpl;
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
{
	public static final Namespace COMMON = Namespace.getNamespace("dust:common");
	
	@Description
	public void configure(Binder binder)
	{
		binder.bind(TemplateManager.class).to(TemplateManagerImpl.class);
		
		binder.bind(TemplateCache.class).to(TemplateCacheImpl.class);
		
		binder.bind(DocumentLinker.class).to(DocumentLinkerImpl.class);
	}
	
	@Contribution
	public void contributeCommonComponents(TemplateManagerImpl registry)
	{
		registry.addComponent(ParameterComponent.class);
		registry.addComponent(BodyComponent.class);
		
		registry.addComponent(IfComponent.class);
		registry.addComponent(LoopComponent.class);
		registry.addComponent(LinkComponent.class);
		registry.addComponent(HolderComponent.class);
		registry.addComponent(RawComponent.class);
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
