# Service Virtualization as Code

CA Service Virtualization as Code (SV as Code) is a new, lightweight way to define virtual services and save valuable time. 

At the core, SV as Code delivers a simple yet powerful Java library that provides an easy to use API so you can create and run virtual services as a part of jUnit testing. 

You can create and run HTTP virtual service definitions directly in your unit testing code.

Even better, the HTTP virtual services are completely transparent to an application under test and so you don't need to make any configuration tweaks.

[How to use SV as Code](https://docops.ca.com/sv-as-code/en)

[Tell us what you think](https://communities.ca.com/community/ca-devtest-community/content?filterID=contentstatus%5Bpublished%5D~category%5Bsv-as-code%5D)


## Code Example
```java
import static com.ca.svcode.protocols.http.fluent.HttpFluentInterface.*;
import static org.junit.Assert.*;

import com.ca.svcode.engine.junit4.VirtualServerRule;
import org.junit.*;

public class ExampleTest {

  @Rule
  public VirtualServerRule vs = new VirtualServerRule();

  @Test
  public void exampleTest() {
    // virtual service definition
    forGet("http://www.example.com/time").doReturn(
        okMessage()
            .withJsonBody("{\"timestamp\":1498838896}")
    );

    // application connects to http://www.example.com/test and retrieves JSON response
    int currentTimestamp = Application.retrieveCurrentTimestamp();

    // received timestamp check
    assertEquals(1498838896, currentTimestamp);
  }
}
```
