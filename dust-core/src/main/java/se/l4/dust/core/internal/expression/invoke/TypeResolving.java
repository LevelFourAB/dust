package se.l4.dust.core.internal.expression.invoke;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.classmate.ResolvedType;

/**
 * Utility methods for working with resolving types.
 *
 * @author Andreas Holstenson
 *
 */
public class TypeResolving
{
	private TypeResolving()
	{
	}

	/**
	 * Find all of the common types for the specified list of types.
	 *
	 * @param types
	 * @return
	 */
	public static List<ResolvedType> findCommonTypes(Collection<ResolvedType> types)
	{
		Set<ResolvedType> allTypes = new HashSet<ResolvedType>();
		final Map<ResolvedType, Integer> typeCount = new HashMap<ResolvedType, Integer>();

		for(ResolvedType type : types)
		{
			while(type != null)
			{
				findTypesOf(type, allTypes, typeCount);

				type = type.getParentClass();
			}
		}

		for(ResolvedType type : types)
		{
			Set<ResolvedType> localTypes = new HashSet<ResolvedType>();
			while(type != null)
			{
				findTypesOf(type, localTypes, null);

				type = type.getParentClass();
			}

			allTypes.retainAll(localTypes);
		}

		ArrayList<ResolvedType> result = new ArrayList<ResolvedType>(allTypes);
		Collections.sort(result, new Comparator<ResolvedType>()
		{
			@Override
			public int compare(ResolvedType o1, ResolvedType o2)
			{
				int result = typeCount.get(o1).compareTo(typeCount.get(o2));
				if(result != 0)
				{
					return result;
				}

				if(o1.isInterface() && ! o2.isInterface())
				{
					return 1;
				}
				else if(! o1.isInterface() && o2.isInterface())
				{
					return -1;
				}
				else if(o1.isAbstract() && ! o2.isAbstract())
				{
					return 1;
				}
				else if(! o1.isAbstract() && o2.isAbstract())
				{
					return -1;
				}
				else
				{
					// Default to name
					return o1.getClass().getName().compareTo(o2.getClass().getName());
				}
			}
		});

	    return result;
	}

	private static void findTypesOf(ResolvedType type, Set<ResolvedType> result, Map<ResolvedType, Integer> typeCount)
	{
		if(typeCount != null)
		{
			Integer count = typeCount.get(type);
			typeCount.put(type, count == null ? 1 : count + 1);
		}

		if(type.getErasedType() != Object.class)
		{
			// Only add specific types
			result.add(type);
		}

		for(ResolvedType rt : type.getImplementedInterfaces())
		{
			findTypesOf(rt, result, typeCount);
		}
	}
}
