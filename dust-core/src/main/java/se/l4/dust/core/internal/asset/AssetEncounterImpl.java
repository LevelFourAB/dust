package se.l4.dust.core.internal.asset;

import se.l4.dust.api.NamespaceManager;
import se.l4.dust.api.NamespaceManager.Namespace;
import se.l4.dust.api.asset.AssetEncounter;
import se.l4.dust.api.resource.Resource;

/**
 * Implementation of {@link AssetEncounter}.
 * 
 * @author Andreas Holstenson
 *
 */
public class AssetEncounterImpl
	implements AssetEncounter
{
	private final NamespaceManager namespaces;
	private final boolean production;
	private final Resource in;
	private final String namespace;
	private final String path;
	
	private Resource replacedWith;
	private String renamedTo;

	public AssetEncounterImpl(NamespaceManager namespaces, boolean production,
			Resource in, String namespace, String path)
	{
		this.namespaces = namespaces;
		this.production = production;
		this.in = in;
		this.namespace = namespace;
		this.path = path;
	}

	@Override
	public Resource getResource()
	{
		return in;
	}

	@Override
	public String getNamepace()
	{
		return namespace;
	}

	@Override
	public Namespace getNamespaceObject()
	{
		return namespaces.getNamespaceByURI(namespace);
	}

	@Override
	public String getPath()
	{
		return path;
	}
	
	@Override
	public boolean isProduction()
	{
		return production;
	}

	@Override
	public AssetEncounter replaceWith(Resource resource)
	{
		replacedWith = resource;
		
		return this;
	}

	@Override
	public AssetEncounter rename(String name)
	{
		renamedTo = name;
		
		return this;
	}

	public Resource getReplacedWith()
	{
		return replacedWith;
	}
	
	public String getRenamedTo()
	{
		return renamedTo;
	}

	public boolean isRenamed()
	{
		return renamedTo != null;
	}

	public boolean isReplaced()
	{
		return replacedWith != null;
	}
}
