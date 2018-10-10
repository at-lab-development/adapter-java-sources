# Test Management Adapter

This dependency gathers basic build information and useful artifacts (titled values, user defined files, screenshoots, stack traces) for further processing by Jenkins Test Management plugin.

## How it works

The key thing is that this adapter are working in tandem with Jenkins Test Management plugin which processes gathered information and updates Jira issues via REST API.

![Scheme](https://github.com/Pavel-Irher/test-management-adapter-java/blob/master/images/readme_scheme.jpg)

## Installation

At this time, Test Management adapter installation is possible only in **local** repository. Unfortunately, the adapter has not yet been placed into the Maven Central Repository.

### Using the command line

```bash
mvn install::install-file -Dfile=test-management-adapter-1.8-jar-with-dependencies.jar 
                          -DgroupId=com.epam.jira 
                          -DartifactId=test-management-adapter 
                          -Dversion=1.8
                          -Dpackaging=jar
```
**For copy-paste:** `mvn install::install-file -Dfile=test-management-adapter-1.8-jar-with-dependencies.jar -DgroupId=com.epam.jira -DartifactId=test-management-adapter -Dversion=1.8 -Dpackaging=jar`

After that you need to add next dependency to your pom-file: 
```bash
<dependency>
    <groupId>com.epam.jira</groupId>
    <artifactId>test-management-adapter</artifactId>
    <version>1.8</version>
</dependency>
```

## Execution Listener

### TestNG

Add `ExecutionListener` to your TestNG listeners by one of the following methods:

### Using _maven-surefire-plugin_ in your pom.xml

```bash
  <build>
      <plugins>
          [...]
          <plugin>
              <groupId>org.apache.maven.plugins</groupId>
              <artifactId>maven-surefire-plugin</artifactId>
              <version>2.20.1</version>
              <configuration>
                  <properties>
                      [...]
                      <property>
                          <name>listener</name>
                          <value>com.epam.jira.testng.ExecutionListener</value>
                      </property>
                      [...]
                  </properties>
              </configuration>
          </plugin>
          [...]
      </plugins>
  </build>
```

### Using _@Listeners_ annotation at class level

```bash
  @Listeners({com.epam.jira.testng.ExecutionListener.class})
  public class TestClass {
      // ...
  }
```

### Using listeners element in _testng.xml_

```bash
  <?xml version="1.0" encoding="UTF-8"?>
  <suite name="Suite" parallel="false">
	  <listeners>
		  <listener class-name="com.epam.jira.testng.ExecutionListener" />
	  </listeners>
	  <test name="Test">
		  <classes>
			  [...]
		  </classes>
	  </test>
  </suite>
```

### Adding listeners through TestNG _addListener()_ API

```bash
  public static void main(String[] args) {
    TestNG testNG = new TestNG();
    testNG.setTestClasses(new Class[] { TestClass.class });
    testNG.addListener(new ExecutionListener());
    testNG.run();
  }
```

### JUnit

Add `ExecutionListener` to your JUnit listeners by one of the following methods:

### Using _maven-surefire-plugin_ in your pom.xml

```bash
  <build>
      <plugins>
          [...]
          <plugin>
              <groupId>org.apache.maven.plugins</groupId>
              <artifactId>maven-surefire-plugin</artifactId>
              <version>2.20.1</version>
              <configuration>
                  <properties>
                      [...]
                      <property>
                          <name>listener</name>
                          <value>com.epam.jira.junit.ExecutionListener</value>
                      </property>
                      [...]
                  </properties>
              </configuration>
          </plugin>
          [...]
      </plugins>
  </build>
```

### Using _exec-maven-plugin_ in your pom.xml

```bash
  <build>
      <plugins>
          [...]
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>exec-maven-plugin</artifactId>
                <version>1.6.0</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>java</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <mainClass>com.epam.talixo.framework.runner.TestRunner</mainClass>
                    <additionalClasspathElements>
                        <additionalClasspathElement>${basedir}/test-management-adapter-1.8-jar-with-dependencies.jar
                        </additionalClasspathElement>
                    </additionalClasspathElements>
                </configuration>
            </plugin>
          [...]
      </plugins>
  </build>
```

### Adding listeners through JUnit _addListener()_ API

```bash
  public static void main(String[] args) {
    JUnitCore jUnitCore = new JUnitCore();
    jUnitCore.addListener(new ExecutionListener());
    jUnitCore.run(TestClass.class);
  }
```

### Using _@Listeners_ annotation at class level

```bash
  @RunWith(com.epam.jira.junit.TestRunner.class)
  public class TestClass {
      // ...
  }
```

## @JIRATestKey
Mark tests with **@JIRATestKey** annotation and specify corresponding issue key as its **key** parameter value.

```bash
  @Test
  @JIRATestKey(key = "EPMFARMATS-1010")
  public void testSomething() {
    Assert.assertTrue(true);
  }
```

You can disable this annotation using its `disabled` option

## Screenshots

You will need to initialize Screenshoter class with WebDriver instance in order to attach screenshots to JIRA issue in the fail cases.

```bash
    @BeforeClass
    public void initialize() {
        Screenshoter.initialize(driver);
    }
```

You can disable screenshots on failure for a certain test using **JiraTestKey** `disableScreenshotOnFailure` option

## Store information

You can store useful informatian such as string values (with titles) or files using **JiraInfoProvider** class.

```bash
    JiraInfoProvider.saveFile(new File("path_to_file"));
    JiraInfoProvider.saveValue("Title", "Some value");
```

## Retry failed tests

You can rerun your failed tests if needed. You can do that by one of the following methods:

### Add AnnotationTransformer to your TestNG Listeners

You can do it in the same way as [Execution Listener](#execution-listener)   :warning: except **@Listeners** annotation

### Using @Test annotation retryAnalyzer property

```bash
    @JIRATestKey(key = "EPMFARMATS-1010")
    @Test(retryAnalyzer = RetryAnalyzer.class)
    public void testSomething() {
        ...
    }
```

If you want to rerun your test several times, you will need to use **JiraTestKey** `retryCountIfFailed` option (default value is **1**). The count of reruns will be desplayed in your Test report summary field.

```bash
    @JIRATestKey(key = "EPMFARMATS-1010", retryCountIfFailed = 2)
    @Test(retryAnalyzer = RetryAnalyzer.class)
    public void testSomething() {
        ...
    }
```

After running the `jira-tm-report.xml` results file with attachments will be created in your project `target` directory.
