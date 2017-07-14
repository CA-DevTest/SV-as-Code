# Service Virtualization as Code

CA Service Virtualization as Code (SV as Code) provides development teams with a new, lightweight way to define virtual services and reduce time to value. SV as Code is built by software developers specifically for other software developers. At the core, SV as Code is a lightweight, powerful Java library that provides a simple to use API so you can create and run virtual services as a part of jUnit testing. 

Create and run HTTP virtual service definitions directly in your unit testing code.

HTTP virtual services are completely transparent to an application under test and do not require any configuration tweaks.

[Tell us what you think](https://communities.ca.com/community/ca-devtest-community/content?filterID=contentstatus%5Bpublished%5D~category%5Bsv-as-code%5D)

[How to use SV as Code](https://docops.ca.com/sv-as-code/en)

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
