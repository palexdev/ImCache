# ImCache

**ImCache** is a lightweight Java library for fetching and caching images from any URL (whether it's a local
file, a web resource, or a custom protocol). It supports in-memory and on-disk caching strategies, as well as optional
image transformations prior to caching.

---

## ✨ Features

- 🔗 Fetch images from local or remote URLs
- 🧠 `MemoryCache`: fast in-memory caching with optional disk sync
- 💾 `DiskCache`: persistent storage for long-term caching
- ♻️ Auto-eviction based on capacity (default: 100 items)
- 🖼️ Supports custom image transformations

---

## 📦 Installation

### Gradle

```groovy
dependencies {
    implementation 'io.github.palexdev:imcache:21.4.0'
}
```

### Maven

```xml

<dependency>
    <groupId>io.github.palexdev</groupId>
    <artifactId>imcache</artifactId>
    <version>21.4.0</version>
</dependency>
```

## 🚀 Getting Started

Here's a generic quick example of how to use ImCache:

```java
ImCache.instance()       // You can also create new instances, constructor is public
    .

cacheConfig(() ->{/*Cache configuration*/})
    .

storeStrategy(...)  // Cache original or transformed image
    .

request(myResource) // Can be a URL, file, string...
    .

transform(...)      // Here you can specify what transform to apply to the image
    .

execute(callback);  // Or executeAsync(...) if you want to load in the background. The callback is optional!
```

## 📝 Documentation

You can read the documentation [here](https://javadoc.io/doc/io.github.palexdev/imcache).

## ©️ License

Distributed under the GNU LGPLv3 License. See LICENSE for more information.