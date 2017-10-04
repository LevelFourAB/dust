package se.l4.dust.validation;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Path;

import se.l4.dust.api.Context;
import se.l4.dust.api.expression.ReflectiveExpressionSource;

import com.google.inject.Inject;
import com.google.inject.Stage;

/**
 * Source of properties and methods that can be used together with the
 * validation mixins.
 *
 * @author Andreas Holstenson
 *
 */
public class ValidationExpressionSource
	extends ReflectiveExpressionSource
{

	@Inject
	public ValidationExpressionSource(Stage stage)
	{
		super(stage);
	}

	@Property
	public boolean error(@Bind Context ctx)
	{
		return find(ctx) != null;
	}

	@Property
	public String message(@Bind Context ctx)
	{
		ConstraintViolation<?> cv = find(ctx);
		return cv == null ? null : cv.getMessage();
	}

	private ConstraintViolation<?> find(Context ctx)
	{
		ConstraintViolation<?> stored = ctx.getValue(ValidationModule.CTX_VIOLATION);
		if(stored != null) return stored;

		String field = ctx.getValue(ValidationModule.CTX_FIELD);
		if(field == null)
		{
			throw new IllegalArgumentException("No field has been bound, did you forget to use the mixin " + ValidationModule.TPL_FIELD + "?");
		}

		Set<ConstraintViolation<?>> violations = ctx.getValue(ValidationModule.CTX_ERRORS);
		if(violations == null || violations.isEmpty())
		{
			return null;
		}

		for(ConstraintViolation<?> cv : violations)
		{
			if(matches(field, cv.getPropertyPath()))
			{
				ctx.putValue(ValidationModule.CTX_VIOLATION, cv);
				return cv;
			}
		}

		return null;
	}

	private static boolean matches(String name, Path path)
	{
		int idx = name.indexOf('.');
		int lastIdx = 0;
		boolean hasMore = true;
		for(Path.Node n : path)
		{
			if(hasMore)
			{
				String sub = idx == -1 ? name : name.substring(lastIdx, idx);
				if(! sub.equals(n.getName()))
				{
					// Not matching, return false
					return false;
				}
			}
			else
			{
				return false;
			}

			lastIdx = idx;
			idx = name.indexOf('.', idx+1);
			hasMore = idx > 0;
		}

		return ! hasMore;
	}
}
