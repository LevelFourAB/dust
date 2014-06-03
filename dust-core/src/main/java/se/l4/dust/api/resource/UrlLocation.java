package se.l4.dust.api.resource;

import java.net.MalformedURLException;
import java.net.URL;

import com.google.common.base.Throwables;

public class UrlLocation
	implements ResourceLocation
{
	private final URL url;

	public UrlLocation(URL url)
	{
		this.url = url;
	}
	
	public URL getUrl()
	{
		return url;
	}
	
	@Override
	public String getName()
	{
		return url.toString();
	}
	
	@Override
	public ResourceLocation withExtension(String newExtension)
	{
		String url = this.url.toString();
		int idx = url.lastIndexOf('.');
		String firstPart = idx > 0 ? url.substring(0, idx) : url;
		try
		{
			return new UrlLocation(new URL(firstPart + "." + newExtension));
		}
		catch(MalformedURLException e)
		{
			throw Throwables.propagate(e);
		}
	}
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "{url=" + url + "}";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		UrlLocation other = (UrlLocation) obj;
		if(url == null)
		{
			if(other.url != null)
				return false;
		}
		else if(!url.equals(other.url))
			return false;
		return true;
	}
}
