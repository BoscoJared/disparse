[![](https://jitpack.io/v/BoscoJared/disparse.svg)](https://jitpack.io/#BoscoJared/disparse) [![Build Status](https://travis-ci.com/BoscoJared/disparse.svg?branch=master)](https://travis-ci.com/BoscoJared/disparse)
[![Documentation Status](https://readthedocs.org/projects/disparse/badge/?version=latest)](https://disparse.readthedocs.io/en/latest/?badge=latest)

# Disparse
An ergonomic, simple, and easy-to-use command parsing and dispatching library for Discord bots.

## Documentation

View the docs on [readthedocs](https://disparse.readthedocs.io/en/latest/)!

## Usage

Disparse is built and served from [Jitpack](https://jitpack.io/#BoscoJared/disparse).

The specific module you want is dependent on the Discord library you would like to use.

- JDA -> `$MODULE` = `disparse-jda`
- D4J -> `$MODULE` = `disparse-d4j`
- SmallD -> `$MODULE` = `disparse-smalld`
- Unsupported / Creating own integration -> `$MODULE` = `disparse-core`

### Maven

Add your repository for Jitpack

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

Add Disparse as a dependency

```xml
<dependency>
    <groupId>com.github.BoscoJared.disparse</groupId>
    <artifactId>$MODULE</artifactId>
    <version>$LATEST</version>
</dependency>
```

### Gradle

Add your repository for Jitpack

```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

Add Disparse as a dependency

```
dependencies {
        implementation 'com.github.BoscoJared.disparse:$MODULE:$LATEST'
}
```
