package se.l4.dust.core.internal.template.dom;

import java.util.List;

import org.jdom.Comment;
import org.jdom.Content;
import org.jdom.DocType;
import org.jdom.JDOMException;
import org.jdom.Text;

import se.l4.dust.api.TemplateFilter;
import se.l4.dust.api.TemplateManager;
import se.l4.dust.api.template.PropertyContent;
import se.l4.dust.dom.Document;
import se.l4.dust.dom.Element;

import com.google.inject.Inject;

/**
 * Class that drives the transformation of a template into a rendered document.
 * 
 * @author Andreas Holstenson
 *
 */
public class TemplateEmitter
{
	private final TemplateManager registry;

	@Inject
	public TemplateEmitter(TemplateManager registry)
	{
		this.registry = registry;
	}
	
	public Document process(Document template, Object data)
		throws JDOMException
	{
		Document doc = new Document();
		
		Element tplRoot = template.getRootElement();
		Element root = null;
		DocType dt = null;
		
		if(tplRoot instanceof TemplateComponent)
		{
			TemplateComponent component = (TemplateComponent) tplRoot;
			FakeElement fakeRoot = new FakeElement();
			
			component.process(this, fakeRoot, data, null, data);
			
			List<Element> children = fakeRoot.getChildren();
			if(children.size() == 1)
			{
				root = children.get(0);
				root.detach();
			}
			else
			{
				throw new JDOMException("Component " + tplRoot 
					+ " returned did not return one element. Return size was: " 
					+ children.size() + "; A document may only have one root element"
				);
			}
			
			// Copy DocType
			dt = fakeRoot.getDocType();
		}
		else
		{
			root = tplRoot.copy();
			
			for(Content c : tplRoot.getContent())
			{
				process(data, root, c, null, data);
			}
		}
		
		
		doc.setRootElement(root);
		
		// Transfer template DocType (overwrites component type)
		if(template.getDocType() != null)
		{
			dt = template.getDocType();
		}
		
		// Set the actual DocType
		if(dt != null)
		{
			doc.setDocType((DocType) dt.clone());
		}
		
		// Apply all filters
		for(TemplateFilter f : registry.getFilters())
		{
			f.filter(doc);
		}
		
		return doc;
	}
	
	public void process(Object data, Element parent, Content in, TemplateComponent lastComponent,
			Object previousRoot)
		throws JDOMException
	{
		if(in instanceof TemplateComponent)
		{
			((TemplateComponent) in).process(this, parent, data, lastComponent, previousRoot);
		}
		else if(in instanceof TemplateElement)
		{
			// Template element support copying with context
			TemplateElement e = (TemplateElement) in;
			
			Element result = e.copy(data);
			parent.addContent(result);

			// Process each child
			for(Content c : e.getContent())
			{
				process(data, result, c, lastComponent, previousRoot);
			}
		}
		else if(in instanceof Element)
		{
			Element e = (Element) in;
			
			Element result = e.copy();
			parent.addContent(result);

			// Process each child
			for(Content c : e.getContent())
			{
				process(data, result, c, lastComponent, previousRoot);
			}
		}
		else if(in instanceof TemplateText)
		{
			// Special case of text, handle property expansions
			TemplateText tt = (TemplateText) in;
			for(Content c : tt.getContent())
			{
				if(c instanceof PropertyContent)
				{
					Object o = ((PropertyContent) c).getValue(data);
					parent.addContent(new Text(String.valueOf(o)));
				}
				else
				{
					parent.addContent((Content) c.clone());
				}
			}
		}
		else if(in instanceof Text)
		{
			parent.addContent((Text) in.clone());
		}
		else if(in instanceof Comment)
		{
			parent.addContent((Comment) in.clone());
		}
	}
}
