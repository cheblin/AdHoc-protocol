**BlackBox** is a multilingual **C, C++, Rust, C#, Kotlin, Typescript...Scala(upcoming)**, low-level binary protocol boilerplate code generator. According to your description, BlackBox generates source code, so you just have to insert your received packs handlers, and logic of creating a new package, populating, with data and sending it to the receiver. 
**BlackBox** supports 
- bitfields,
- standart set primitive datatypes, 
- UTF8 strings everywhere
- variations of its multidimensional fields with predefined and variable dimensions
- nested packs,
- ordinary and flagbits like enums 
- pack binded constats
- pack fields inheritance 

To transmit data via the radio channel / Bluetooth / UART, [byte stuffing framing](https://web.cs.wpi.edu/~rek/Undergrad_Nets/B07/BitByteStuff.pdf) and CRC are built into the AdvChannel entity. 
In the case of sending over secure transport or if BlackBox used as a serialization tool of the program data in a file, the StdChannel is using. Booth channel version heavy use [Base 128 Varint](https://developers.google.com/protocol-buffers/docs/encoding) compression. <br>

At the moment, the code generator BlackBox is built like **SaaS**. To get the generated and tested code it is necessary:

- Create a protocol specification. In fact, this is a plain java file. [Here is an example](https://github.com/cheblin/BlackBox_LEDBlink_Demo/blob/master/org/unirail/demo/LedBlinkProject.java) of how it looks for a demo project to [control from Android](https://github.com/cheblin/BlackBox_LEDBlink_Demo/tree/master/Examples/Android) a blinking LED on [the board under STM8S103F3P6](https://github.com/cheblin/BlackBox_LEDBlink_Demo/tree/master/Examples/STM8) via Bluetooth UART on HC 08.
<br>While writing a specification, it is necessary in java project to make a refference to the [set of annotations describing data characteristics](https://github.com/cheblin/BlackBox/tree/master/org/unirail/BlackBox). <br>And following a [small set of rules](http://www.unirail.org/), describe packets, channels, Hosts, communication interfaces, network topology.
- Verify that the specification is successfully compiled, and checkout [ClientAgent](https://github.com/cheblin/ClientAgent) rename one of .properties file to client.properties change it content according you email account. Run ClientAgent and in a few second/minutes get generated code, next to your protocol description. You will get generated source code, according to specification, in requested programming languages, last passed test and an example of using generated API. If an error occurred, you will be notified of a possible delay and the BlackBox support service is dealing with the difficulties encountered.

[Here you can find an example of the generated code](https://github.com/cheblin/BlackBox_LEDBlink_Demo/tree/master/Generated), and here an [example of usage of this code](https://github.com/cheblin/BlackBox_LEDBlink_Demo/tree/master/Examples/STM8) in the above-mentioned demonstration control project with android LED flashes on the demo PCB assembled on STM8.

Using BlackBox, you can easily establish communication not only between microcontrollers, mobile devices but also between the ordinary computers. And what is important, without time and effort waste. In fact, the generated BlackBox code can become the skeleton of your distributed application. The programmer will just have to add handlers to the packet&#39;s reception events, as well as the logic for creating the package, populating it with data and sending it to the recipient.


# BlackBox description file
Basic documentation of the description file format can be found **[here](http://www.unirail.org).** Let's take a look how [**LedBlinkProject** demo description file](https://github.com/cheblin/BlackBox_LEDBlink_Demo) looks like
![descriptionscheme](http://www.unirail.org/wp-content/uploads/2017/12/Capture2.png)

# BlackBox parts relationship scheme

![OverallScheme](http://www.unirail.org/wp-content/uploads/2017/12/OverallScheme.png)

# How to get generated source code

1. Install **Intellij** [IDEA](https://www.jetbrains.com/idea/download/#section=windows) or, if you are planning to deploy your code on Android devices, [Android Studio.](https://developer.android.com/studio/index.html)
2. Download [BlackBox metadata annotations](https://github.com/cheblin/BlackBox/tree/master/org/unirail/BlackBox)
3. Create a new java Project in your IDE and make reference to downloaded metadata annotations. (On **Android Studio** you have to [add JAVA Library module](https://developer.android.com/studio/projects/android-library.html) or edit  [build.gradle](https://github.com/cheblin/BlackBox_LEDBlink_Demo/blob/master/Examples/Android/app/build.gradle) file. Find/add **java.srcDirs** option.)
3. Compose your protocol description file (it should be in UTF8 encoding). **[Rules.](http://www.unirail.org/)**
4. Ensure that description file can me compiled without any errors.
5. Upload your protocol descriptor file with [ClientAgent](https://github.com/cheblin/ClientAgent)
6. In a short time getting your generated, fully tested source code in reply.<br>
In addition it will contain **Demo and Test** file - examples of using generated API and one of the passsed test, respectively.
