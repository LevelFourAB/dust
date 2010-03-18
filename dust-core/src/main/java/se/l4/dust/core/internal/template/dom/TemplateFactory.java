package se.l4.dust.core.internal.template.dom;

import org.jdom.Attribute;
import org.jdom.Comment;
import org.jdom.Namespace;
import org.jdom.Text;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import se.l4.dust.api.TemplateManager;
import se.l4.dust.dom.Element;
import se.l4.dust.dom.XJDOMFactory;

@Singleton
public class TemplateFactory
	extends XJDOMFactory
{
	private final TemplateManager manager;
	private final TemplateComponentBuilder builder;

	@Inject
	public TemplateFactory(TemplateManager manager, TemplateComponentBuilder builder)
	{
		this.manager = manager;
		this.builder = builder;
	}

	@Override
	public void addNamespaceDeclaration(org.jdom.Element parent, Namespace additional)
	{
		if(false == manager.isComponentNamespace(additional))
		{
			super.addNamespaceDeclaration(parent, additional);
		}
	}
	
	@Override
	public Element element(String name, Namespace namespace)
	{
		if(manager.isComponentNamespace(namespace))
		{
			return builder.build(namespace, name);
		}
		
		return new TemplateElement(name, namespace);
	}
	
	@Override
	public Element element(String name, String prefix, String uri)
	{
		Namespace ns = Namespace.getNamespace(prefix, uri);
		return element(name, ns);
	}
	
	@Override
	public Element element(String name)
	{
		return new TemplateElement(name);
	}
	
	@Override
	public Element element(String name, String uri)
	{
		Namespace ns = Namespace.getNamespace(uri);
		return element(name, ns);
	}
	
	@Override
	public Attribute attribute(String name, String value)
	{
		return new TemplateAttribute(name, value);
	}
	
	@Override
	public Attribute attribute(String name, String value, int type)
	{
		return new TemplateAttribute(name, value, type);
	}
	
	@Override
	public Attribute attribute(String name, String value, int type,
			Namespace namespace)
	{
		return new TemplateAttribute(name, value, type, namespace);
	}
	
	@Override
	public Attribute attribute(String name, String value, Namespace namespace)
	{
		return new TemplateAttribute(name, value, namespace);
	}
	
	@Override
	public void setAttribute(org.jdom.Element parent, Attribute a)
	{
		if(parent instanceof TemplateComponent)
		{
			a = new TemplateAttribute(a.getName(), a.getValue(), a.getNamespace());
		}
		
		super.setAttribute(parent, a);
	}
	
	@Override
	public Text text(String text)
	{
		return new TemplateText(text);
	}
	
	@Override
	public Comment comment(String text)
	{
		return new TemplateComment(text);
	}
}
