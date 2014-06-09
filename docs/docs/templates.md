---
layout: page
title: Templates

nav: docs
---

Templates are a central concept in Dust. The template engine is based on XML-input and outputs clean HTML. By default templates share the same name as their class files but ending with `.xml`.

For example the Java-class `org/example/Test.java` would have its template in `org/example/Test.xml`.

A template might look like this:

{% highlight xml %}
<d:loop xmlns:d="dust:common"
	source="${values}" value="${value}">
	${value}<br />
</d:loop>
{% endhighlight %}

## Use with JAX-RS

Any JAX-RS resource that returns an object annotated with `@Template` will be rendered via its template.

## Components and fragments

Templates can contain fragments from other namespaces. Most of these namespaces are actually components. Dust provides a limited set of fragments and components in `dust:common`. 

Those components are:

* `if` - if-statement.
* `loop` - loop over some values outputing its content for every value.
* `raw`- outputs its value as raw HTML without any filtering
* `holder` - wraps some content and outputs it
* `render` - render an object in the template
* `body` - outputs the body or a parameter of a component
