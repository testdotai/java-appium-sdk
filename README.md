[![test.ai sdk logo](https://testdotai.github.io/static-assets/logo-sdk.png)](https://adoptium.net)

[![JDK-11+](https://img.shields.io/badge/JDK-11%2B-blue)](https://adoptium.net)
[![Apache 2.0](https://img.shields.io/badge/Apache-2.0-blue)](https://www.apache.org/licenses/LICENSE-2.0)

The test.ai Appium SDK is a simple library that makes it easy to write robust cross-platform mobile application tests backed by computer vision and artificial intelligence.

test.ai integrates seamelessly with your existing tests, and will act as backup if your selectors break/fail by attempting to visually (computer vision) identify elements.  This ability can also be leveraged to to write a single test suite that works on both iOS & Android.

The test.ai SDK is able to accomplish this by automatically ingesting your Appium elements (using both screenshots and element names) when you run your test cases with test.ai for the first time. 

The SDK is accompanied by a [web-based editor](https://sdk.test.ai/) which makes building visual test cases easy; you can draw boxes around your elements instead of using fragile CSS or XPath selectors.

## Install

Add the following line(s) to the dependencies section in your

**pom.xml (Maven)**
```xml
<dependency>
  <groupId>ai.test.sdk</groupId>
  <artifactId>test-ai-appium</artifactId>
  <version>0.0.2</version>
</dependency>
````

**build.gradle (Gradle)**
```groovy
implementation 'ai.test.sdk:test-ai-appium:0.0.2'
```

## Tutorial
We have a detailed step-by-step tutorial which will help you get set up with the SDK: https://github.com/testdotai/java-appium-sdk-demo

## Resources
* [API Docs](https://testdotai.github.io/java-appium-sdk/)
* [Another Tutorial](https://sdk.test.ai/tutorial)