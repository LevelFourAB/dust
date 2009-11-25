package se.l4.dust;

import javax.ws.rs.Produces;

import org.jdom.Namespace;

/**
 * Set of usable properties and methods that are common throughout applications
 * using Dust.
 * 
 * @author Andreas Holstenson
 *
 */
public class Dust
{
	/** 
	 * Standard content type for HTML pages for use with {@link Produces}.
	 * Charset encoding will be set to UTF-8. 
	 */
	public static final String HTML = "text/html; charset=UTF-8";
	
	/**
	 * Parameter flag for defining if production mode should be used. This
	 * should be set in the {@code web.xml} of the application to {@code true}
	 * before deployment.
	 */
	public static final String DUST_PRODUCTION = "dust.production";
	
	/**
	 * Context namespace, used to refer to files in the webapp context.
	 */
	public static final Namespace CONTEXT_NAMESPACE = Namespace.getNamespace("ctx", "dust:context");
	
	private Dust()
	{
	}
}