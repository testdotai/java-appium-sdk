[![JDK-11+](https://testdotai.github.io/static-assets/logo-sdk.png)](https://adoptium.net)

[![JDK-11+](https://img.shields.io/badge/JDK-11%2B-blue)](https://adoptium.net)
[![Apache 2.0](https://img.shields.io/badge/Apache-2.0-blue)](https://www.apache.org/licenses/LICENSE-2.0)

The test.ai SDK is a seamless and painless way to AI-ify your Appium test cases by leveraging AI (specifically computer vision).

It will automatically ingest your data (screenshots and element names) as your run the test cases for the first time. After that the AI will act as back up when the selectors are broken and try to visually identify elements.

Simply put, the test.ai SDK can help you create cross-platform test cases that work for both iOS and Android with the same elements.  No more breaking tests!

The SDK also comes with a built-in editor to help you build test cases visually, so you can draw boxes around your elements instead of using fragile CSS or XPath selectors.

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
We have a great step-by-step tutorial which will help you get set up with the SDK so you can hit the ground running with the test.ai sdk!

Check it out: https://github.com/testdotai/java-appium-sdk-demo

## Resources
* [API Docs](https://testdotai.github.io/java-appium-sdk/)
* [Another Tutorial](https://sdk.test.ai/tutorial)