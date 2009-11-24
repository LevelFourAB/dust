package se.l4.dust.core.internal.asset;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;

import org.jdom.Namespace;

import com.google.inject.Inject;

import se.l4.dust.api.asset.AssetSource;

public class ContextAssetSource
	implements AssetSource
{
	public static final Namespace NAMESPACE = Namespace.getNamespace("ctx", "dust:context");
	
	private final ServletContext ctx;
	
	@Inject
	public ContextAssetSource(ServletContext ctx)
	{
		this.ctx = ctx;
	}
	
	public URL locate(Namespace ns, String pathToFile)
	{
		if(NAMESPACE.equals(ns))
		{
			try
			{
				return ctx.getResource("/" + pathToFile);
			}
			catch(MalformedURLException e)
			{
			}
		}
		
		return null;
	}

}
