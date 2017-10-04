package se.l4.dust.core.internal;

import java.util.concurrent.ExecutionException;

import com.google.common.base.Throwables;
import com.google.common.cache.AbstractLoadingCache;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.Stage;

@Singleton
public class Caches
{
	private Stage stage;

	@Inject
	public Caches(Stage stage)
	{
		this.stage = stage;
	}

	public <K, V> Cache<K, V> newCache()
	{
		return newCache(null);
	}

	public <K, V> Cache<K, V> newCache(CacheConfig config)
	{
		if(stage == Stage.DEVELOPMENT)
		{
			return new DevelopmentCache<K, V>(null);
		}
		else
		{
			CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder();

			if(config != null)
			{
				config.configure(builder);
			}

			return builder.build();
		}
	}

	public <K, V> LoadingCache<K, V> newLoadingCache(CacheLoader<K, V> loader)
	{
		return newLoadingCache(loader, null);
	}

	public <K, V> LoadingCache<K, V> newLoadingCache(CacheLoader<K, V> loader, CacheConfig config)
	{
		if(stage == Stage.DEVELOPMENT)
		{
			return new DevelopmentCache<K, V>(loader);
		}
		else
		{
			CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder();

			if(config != null)
			{
				config.configure(builder);
			}

			return builder.build(loader);
		}
	}

	private static class DevelopmentCache<K, V>
		extends AbstractLoadingCache<K, V>
	{
		private final CacheLoader<K, V> loader;

		public DevelopmentCache(CacheLoader<K, V> loader)
		{
			this.loader = loader;
		}

		@Override
		public V get(K key)
			throws ExecutionException
		{
			try
			{
				return loader.load(key);
			}
			catch(Exception e)
			{
				Throwables.propagateIfInstanceOf(e, ExecutionException.class);
				throw new ExecutionException(e);
			}
		}

		@Override
		public void put(K key, V value)
		{
		}

		@Override
		public V getIfPresent(Object key)
		{
			return null;
		}
	}

	public interface CacheConfig
	{
		void configure(CacheBuilder<?, ?> build);
	}
}
