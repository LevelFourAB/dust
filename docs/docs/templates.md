---
layout: page
title: Templates

nav: docs
---

Templates is how Dust renders pages and components. The template engine is based on XML-input and outputs clean HTML. By default templates share the same name as their class files but ending with `.xml`.

For example the Java-class `org/example/Test.java` would have its template in `org/example/Test.xml`.

A template might look like this:

{% highlight xml %}
<d:loop xmlns:d="dust:common"
	source="${values}" value="${value}">
	${value}<br />
</d:loop>
{% endhighlight %}

With this class to provide its values:

{% highlight java %}
public class Test {
	public String[] getValues() {
		return new String[] { "One", "Two", "Three" };
	}

	public String getValue() {
		return values;
	)

	public void setValue(String value) {
		this.value = value;
	}
}
{% endhighlight %}

## Use with JAX-RS

Any JAX-RS resource that returns an object annotated with `@Template` will be rendered via its template.

## Components

Templates can reference components from other namespaces. Dust provides a standard set of components in <a href="{{ site.baseurl }}/docs/namespaces/common/">dust:common</a>.

<a href="{{ site.baseurl }}/docs/templates/components/" class="btn btn-lg btn-outline">Read more about components</a>

