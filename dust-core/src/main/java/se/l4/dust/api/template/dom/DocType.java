package se.l4.dust.api.template.dom;

/**
 * Document type declaration.
 *
 * @author Andreas Holstenson
 *
 */
public class DocType
{
	private final String name;
	private final String publicId;
	private final String systemId;

	public DocType(String name, String publicId, String systemId)
	{
		this.name = name;
		this.publicId = publicId;
		this.systemId = systemId;
	}

	public String getName()
	{
		return name;
	}

	public String getPublicId()
	{
		return publicId;
	}

	public String getSystemId()
	{
		return systemId;
	}
}
