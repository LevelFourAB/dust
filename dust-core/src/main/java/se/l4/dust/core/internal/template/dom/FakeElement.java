package se.l4.dust.core.internal.template.dom;

import org.jdom.DocType;

import se.l4.dust.dom.Element;

/**
 * Fake element used when a template component is to be emitted as the root
 * element. This is used to transfer the {@link DocType} from components.
 * 
 * @author Andreas Holstenson
 *
 */
public class FakeElement
	extends Element
{
	private DocType docType;
	
	public FakeElement()
	{
	}
	
	public DocType getDocType()
	{
		return docType;
	}
	
	public void setDocType(DocType docType)
	{
		this.docType = docType;
	}
}
