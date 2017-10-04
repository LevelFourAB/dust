package se.l4.dust.core.internal.asset;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.google.common.io.CountingInputStream;

import se.l4.dust.api.asset.AssetCache;
import se.l4.dust.api.resource.Resource;

/**
 * Encapsulation of resources in cached form.
 *
 * @author Andreas Holstenson
 *
 */
public class CacheFormat
{
	private final int length;
	private final String contentType;
	private final String contentEncoding;
	private final long startOfFile;
	private final AssetCache cache;
	private final String key;
	private final long lastModified;

	private CacheFormat(AssetCache cache, String key, int length, String contentType, String contentEncoding, long lastModified, long startOfFile)
	{
		this.cache = cache;
		this.key = key;
		this.length = length;
		this.contentType = contentType;
		this.contentEncoding = contentEncoding;
		this.lastModified = lastModified;
		this.startOfFile = startOfFile;
	}

	public int getLength()
	{
		return length;
	}

	public String getContentType()
	{
		return contentType;
	}

	public String getContentEncoding()
	{
		return contentEncoding;
	}

	public long getLastModified()
	{
		return lastModified;
	}

	public InputStream openStream()
		throws IOException
	{
		InputStream stream = cache.get(key);
		stream.skip(startOfFile);
		return stream;
	}

	public static CacheFormat fromCachedStream(AssetCache cache, String key)
		throws IOException
	{
		InputStream stream = cache.get(key);
		if(stream == null) return null;

		try
		{
			CountingInputStream counting = new CountingInputStream(stream);
			int version = counting.read();

			int length = readInteger(counting);
			String contentType = readString(counting);
			String contentEncoding = readString(counting);
			long lastModified = readLong(counting);

			long startOfFile = counting.getCount();

			return new CacheFormat(cache, key, length, contentType, contentEncoding, lastModified, startOfFile);
		}
		finally
		{
			Closeables.closeQuietly(stream);
		}
	}

	private static int readInteger(InputStream stream)
		throws IOException
	{
		int shift = 0;
		int result = 0;
		while(shift < 32)
		{
			final byte b = (byte) stream.read();
			result |= (b & 0x7F) << shift;
			if((b & 0x80) == 0) return result;

			shift += 7;
		}

		return result;
	}

	private static long readLong(InputStream in)
		throws IOException
	{
		int shift = 0;
		long result = 0;
		while(shift < 64)
		{
			final byte b = (byte) in.read();
			result |= (long) (b & 0x7F) << shift;
			if((b & 0x80) == 0) return result;

			shift += 7;
		}

		throw new EOFException("Invalid long");
	}

	private static String readString(InputStream stream)
		throws IOException
	{
		int length = readInteger(stream);
		byte[] buf = new byte[length];
		ByteStreams.readFully(stream, buf);
		return buf.length == 0 ? null : new String(buf);
	}

	public static void store(Resource resource, AssetCache cache, String key)
		throws IOException
	{
		OutputStream out = cache.store(key);
		InputStream in = resource.openStream();
		try
		{
			out.write(1);

			writeInteger(out, resource.getContentLength());
			writeString(out, resource.getContentType());
			writeString(out, resource.getContentEncoding());
			writeLong(out, resource.getLastModified());

			ByteStreams.copy(in, out);
		}
		finally
		{
			out.close();
			Closeables.closeQuietly(in);
		}
	}

	private static void writeInteger(OutputStream out, int value)
		throws IOException
	{
		while(true)
		{
			if((value & ~0x7F) == 0)
			{
				out.write(value);
				break;
			}
			else
			{
				out.write((value & 0x7f) | 0x80);
				value >>>= 7;
			}
		}
	}

	private static void writeLong(OutputStream out, long value)
		throws IOException
	{
		while(true)
		{
			if((value & ~0x7FL) == 0)
			{
				out.write((int) value);
				break;
			}
			else
			{
				out.write(((int) value & 0x7f) | 0x80);
				value >>>= 7;
			}
		}
	}

	private static void writeString(OutputStream out, String value)
		throws IOException
	{
		if(value == null) value = "";

		byte[] data = value.getBytes();
		writeInteger(out, data.length);
		out.write(data);
	}
}
