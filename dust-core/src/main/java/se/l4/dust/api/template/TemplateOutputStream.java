package se.l4.dust.api.template;

import java.io.Closeable;
import java.io.IOException;

/**
 * Template output stream that receives a stream of events and should transform
 * them into a suitable output format.
 *  
 * @author Andreas Holstenson
 *
 */
public interface TemplateOutputStream
	extends Closeable
{
	/**
	 * Output a document type declaration. Output streams should take care to
	 * only output the doc type where it is legal as emitters can call this
	 * method at any time.
	 * 
	 * @param name
	 * @param publicId
	 * @param systemId
	 * @throws IOException 
	 */
	void docType(String name, String publicId, String systemId)
		throws IOException;
	
	/**
	 * Output a new element.
	 * 
	 * @param name
	 * @param attributes
	 * @param close
	 * @throws IOException
	 */
	void startElement(String name, String[] attributes)
		throws IOException;
	
	/**
	 * Output a new element.
	 * 
	 * @param name
	 * @param attributes
	 * @param close
	 * @throws IOException
	 */
	void startElement(String name, String[] attributes, boolean close)
		throws IOException;
		
	/**
	 * End the current element.
	 * 
	 * @param name
	 * @throws IOException
	 */
	void endElement(String name)
		throws IOException;
	
	/**
	 * Start outputting a comment.
	 * 
	 * @throws IOException
	 */
	void startComment()
		throws IOException;
	
	/**
	 * End outputting of a comment.
	 * 
	 * @throws IOException
	 */
	void endComment()
		throws IOException;
	
	/**
	 * Output text either in an element or in a comment.
	 * 
	 * @param text
	 * @throws IOException
	 */
	void text(String text)
		throws IOException;

	/**
	 * Output raw contents that should not be encoded.
	 * 
	 * @param text
	 * @throws IOException
	 */
	void raw(String text)
		throws IOException;
}
