package se.l4.dust.api.discovery;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Set;

public interface DiscoveryEncounter
{
	Collection<Class<?>> getAnnotatedWith(Class<? extends Annotation> annotation);
}
