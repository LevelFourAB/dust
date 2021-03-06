package se.l4.dust.api;


public class Values
{
	private Values()
	{
	}

	public static <T> Value<T> of(T value)
	{
		return new StaticValue<>(value);
	}

	public static class StaticValue<T>
		implements Value<T>
	{
		private final T value;

		private StaticValue(T value)
		{
			this.value = value;
		}

		@Override
		public T get(Context context, Object data)
		{
			return value;
		}

		@Override
		public boolean supportsGet()
		{
			return true;
		}

		@Override
		public void set(Context context, Object data, Object value)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean supportsSet()
		{
			return false;
		}

		@Override
		public Class<? extends T> getType()
		{
			return (Class<? extends T>) value.getClass();
		}
	}
}
