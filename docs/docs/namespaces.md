---
layout: page
title: Namespaces

nav: docs
---

A central concept in Dust are namespaces, which is how it's possible to reference components and other resources in one module from another one.

Namespaces take the form of an URI and follow the same rules as <a href="http://en.wikipedia.org/wiki/XML_namespace">XML namespaces</a>.

Modules register namespaces during their initialization, which causes the module to be scanned for any fragments, components and pages within the package of the module.

## A simple module

{% highlight java %}
public static class MyAppModule extends CrayonModule {
	@Override
	protected void configure() {
	}

	@NamespaceBinding
	public void contributeNamespace(Namespaces namespaces) {
		namespaces.bind("dust:example").add();
	}
}
{% endhighlight %}

This module binds the namespace `dust:example` to the package of the class `MyAppModule`.

## Referencing a namespace

It is now possible to reference any component discovered in the namespace by declaring the namespace in your template XML:

{% highlight xml %}
<div xmlns:app="dust:example">
	<app:myComponent /> <!-- references myComponent in dust:example -->

	${app:echo('hello')} <!-- calls the method echo in dust:example -->
</div>
{% endhighlight %}

## Built-in namespaces

By default a few namespaces are registered, these are:

* <a href="{{ site.baseurl }}/docs/namespaces/common/">dust:common</a> - common template components
* <a href="{{ site.baseurl }}/docs/templates/components/#using-components">dust:parameters</a> - for passing blocks of markup into components
* dust:fragments - define tiny reusable blocks of markup within a template
* dust:messages - holds localized messages of the current template
