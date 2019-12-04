Manual writing of serialization and data deserialization code, especially between heterogeneous units, in different programming languages, 
is a time- consuming process and error-prone. The most efficient solution is to create a DSL – by means of which formally describes the protocol, 
and then create a utility that generates the source code based on this description for various target platforms, in the required programming languages.  

Some examples of solutions build in this way:  
[Protocol Buffers ](https://developers.google.com/protocol-buffers/docs/overview)  
[Cap’n Proto ](https://capnproto.org/language.html)  
[FlatBuffers ](http://google.github.io/flatbuffers/flatbuffers_guide_writing_schema.html)  
[ZCM ](https://github.com/ZeroCM/zcm/blob/master/docs/tutorial.md)  
[MAVLink ](https://github.com/mavlink/mavlink)  
[Thrift](https://thrift.apache.org/docs/idl)

Having studied these, and many other solutions, I have decided to create a system that will implement and complement the merits, 
eliminating the discovered shortcomings.

**AdHoc** protocol is a multilingual **C, C++, Rust, C#, Kotlin, Typescript...Scala/GO(upcoming)**, low-level binary protocol boilerplate code generator.
 According to your description file, **AdHoc** server generates source code, so you just have to insert your received packs handlers, and logic of creating a 
 new package, populating, with data and sending it to receiver.   
**AdHoc** supports:
- bitfields,
- standard set primitive datatype, 
- UTF8 strings using everywhere
- optional fields
- variations of multidimensional fields with predefined and variable dimensions
- nested packs,
- ordinary and flagbits like enums 
- pack constants
- pack fields inheritance 
- generate code adapter to simplify fast exchange data between user objects and  protocol packs 
- [Base 128 Varint](https://developers.google.com/protocol-buffers/docs/encoding) compression.

To transmit data via the radio channel / raw UART, **AdHoc** protocol has built-in AdvChannel entity with [byte stuffing framing](https://web.cs.wpi.edu/~rek/Undergrad_Nets/B07/BitByteStuff.pdf) and CRC.
 
In the case of sending over secure transport or if AdHoc used as a serialization tool of the program data in a file, the StdChannel is using. Booth channel version heavy  <br>

At the moment, the code generator AdHoc is built like **SaaS**. To get the generated and tested code it is necessary:

-   install **[JDK 8](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)** and
    ensure that `javac` (java compiler) is in PATH and accessible from the console.
-   install any **JAVA** IDE (**[Intellij IDEA](https://www.jetbrains.com/idea/download/)** – just fine)
-   download [AdHoс protocol annotations](https://github.com/cheblin/AdHoc-protocol/tree/master/org/unirail/AdHoc).
    All AdHoc protocol description projects will need a reference to these
    annotations, precisely to the folder where the `org` - the annotations root
    folder is located. Since annotations are referenced as sources, in **IDEA**, you
    have to add them to the project in this way [Add Content
    Root](https://www.jetbrains.com/help/idea/content-roots.html).
-   To send completed protocol description file to the server (to generate of
    AdHoc protocol handler source code) and receive the result, you will need the
    **[AdHocAgent](https://github.com/cheblin/AdHocAgent)** utility. Please download
    the [precompiled jar](https://github.com/cheblin/AdHocAgent/tree/master/bin)
    or compile the utility from provided source by yourself.


In fact AdHoc protocol description file, is a plain java file. So just create **JAVA** project in your favorite IDE and add reference to [AdHoc protocol annotations](https://github.com/cheblin/AdHoc-protocol/tree/master/src/org/unirail/AdHoc)  
and import them in description file with **import org.unirail.AdHoc.\*;**.  

This annotation provides additional meta-information for the code generator.

# Numeric fields value changing dispersion 

The pack numeric fields annotations \@A, \@V, \@X, \@I are denoting the
meta-information about the pattern of the field value changing. Based on this
annotation information code generator can ignore or apply [Base 128 Varint](https://developers.google.com/protocol-buffers/docs/encoding)  compression
algorithm, which allows well and with minimal load, reduces the sending data
amount. This is achieved by excluding from transmission of the higher, not
filled bytes but restoring them on the receiving side.

This graph shows the dependence of sending bytes on transferred value

![image](https://user-images.githubusercontent.com/29354319/70126207-84ba9980-16b3-11ea-9900-48251b545eef.png)

It is became clear that with Base 128 Varint encoding, smaller value require less bytes to transfer.
As protocol creator, you know your data, and annotations are the means by which you pass your knowledge to the code generator.
For example, numeric data can have random values, uniformly distributed in full range. Almost as noise.

![image](https://user-images.githubusercontent.com/29354319/70127303-bdf40900-16b5-11ea-94c9-c0dcd045500f.png)

The compression or encoding of this type of data is wasting computation resources. This kind of data is better to transmit as-is.

This kind numeric fields should not have any value dispersion annotation or have `@I` if needed.

Other numeric fields type can have some dispersion/gradient pattern in its value changing

![image](https://user-images.githubusercontent.com/29354319/70128574-0a404880-16b8-11ea-8a4d-efa8a7358dc1.png)

Three main types of distribution of numeric field value changing can be highlighted

| pattern             |  description 
:-------------------------:|:-------------------------
![image](https://user-images.githubusercontent.com/29354319/70131681-afa9eb00-16bd-11ea-9fcc-c6637d80114c.png)|Rare fluctuations are possible only in the direction of bigger values relative to most probable value `val`. These numeric field is annotated with  `@A`
![image](https://user-images.githubusercontent.com/29354319/70130976-7d4bbe00-16bc-11ea-946a-cfa533a09efd.png)|Rare fluctuations are possible in both directions relative to most probable value `val`. These numeric field is annotated with`@X`
![image](https://user-images.githubusercontent.com/29354319/70131118-bbe17880-16bc-11ea-84a2-2a2a4106a810.png)|Fluctuations are possible only in the direction of smaller values relative to most probable value `val`.These numeric field is annotated with  `@V`


The most probable value  – **val** is passed as  annotation argument 


### Using example
| **Language construct**    | **Description**                                                                                                                                                     |
|:--------------------------|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| @I byte field             | mandatory field, the field data before sending is not encoded (poorly compressible), and can take values in the range from **-128** to **127**                      |
| @A byte field            | mandatory field, the data is compressed, the field can take values in the range from **0** to **255**. In fact it is an analogy to the type **uint8_t** in **C.** |
| @I (-1000) byte field    | mandatory field, (not to be compressed), the field can take values in the range from **-1128** to **-873**                                                          |
| @X_ short field          | The **nullable(optional)** field takes values in the range  from **-32 768** to **32 767**. will be compressed with ZigZag on sending.                                       |
| @A (1000) short field    | mandatory field takes a value between – **1 000** to **65 535** will be compressed on sending.                                                           |
| @V_ short field          | **nullable** field takes a value between    **-65 535**  to **0**    will be compressed on sending.                                                                 |
| @I(-11/75) short field   | Required field with uniformly distributed values in the specified range.                                                                                            |

 

# Multidimensional fields

The following annotations are used to describe multidimensional fields

|  |  |
|-----|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `@D`  | All space for field data is allocated in advance as field initialized.  Used in a case when it is known that the array is most likely to be completely filled with data. Even if the data is not set – the  space for it is allocated, but there is no resource wasted on tracking the fullness of the data. |
| `@D_` | Space for data is allocated only when actually data inserted. Used for **sparse** arrays, when it is known that the array is most likely to be poorly filled.  There are additional costs associated with tracking the fullness of the data.     | 

| .................................Example................................| **Description**                                                                                                                                                                                                                                |
|:-------------------------------------|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `@D(1\|2\|3)int field1;`      | Mandatory multidimensional array with predefined dimensions  **1 x 2 x 3.**   Returns primitives.                                                                                                                                                                         |
| `@D(1 \| 2\| 3) int []  field1;`   | Multidimensional array with predefined dimensions   **1 x 2**  Returns arrays of **predefined **length **3**.                                                                                                                                    |
| `@D(1\|2\|-3)  int []  field1;`  | A multidimensional array with predefined dimensions    **1 x 2**   Returns arrays of **equal**,  variable (  **1** to  **3** ) length                                                                                                                   |
| `@D(1\|2\|~3)  int []  field1;` | Multidimensional array with predefined dimensions **1 x 2**   Returns arrays of **different,** variable (  **1** to  **3** ) length                                                                                                           |
| `@A \@D( 1 \| 2 \| 3 ) byte field;`   | **Required** field multidimensional array with predefined dimensions **1 x 2 x 3.**  Returns primitives with uneven distribution of values upward.                                                                                                     |
| \@A\_ \@D( 1 \| 2 \| 3 ) byte field | **Optional** field is a multidimensional array with predefined dimensions of **1 x 2 x 3.**  When an array is created, all the necessary space is allocated.  Returns primitives with unequal distribution of values upward.                                                             |
| \@A(337) String field               | Returns a string with a maximum length of 337 **2-bytes per** **characters**. (An annotation  \@V  denote a string **1-byte** characters string)                                                                                                                  |
| \@V String [] field                 | Returns the contents of the string – an array of** single-byte** characters. The maximum length of lines is up to 127 characters.                                                                                                              |
| \@X_(3 / 45) @D( 12) byte [] field | **Optional** field returns an array of a predefined length  **12.** The values of the array are in a given range, with uneven distribution in both directions relative to the middle of the range.                                             |
| \@D(-45) int [] field               | **Optional** field.  Returns an array of lengths from **1** to **45**                                                                                                                                                                                               |
| \@B( 3 ) byte field                 | Mandatory bit field. Field length 3 bits                                                                                                                                                                                                       |
| \@B_( 12 \| 67 ) byte field         | **Optional** bit field. The length of the field in bits will be calculated based on the transmitted range of allowed values.                                                                                                                   |
| \@D_(1 \| -2 \| -3)  int  field1;   | A multidimensional array with a predetermined first dimension **1 **while other dimensions are variable. The place for the data, within the maximum values of the dimensions, is allocated only as it is added to the array.   Return primitives.                 |



In addition to optimizing traffic, ** BlackBox  **allows you










And following a [small set of rules](http://www.unirail.org/), describe packets, channels, Hosts, communication interfaces, network topology.
- Verify that the specification is successfully compiled, and checkout [ClientAgent](https://github.com/cheblin/ClientAgent) rename one of .properties file to client.properties change it content according you email account. Run ClientAgent and in a few second/minutes get generated code, next to your protocol description. You will get generated source code, according to specification, in requested programming languages, last passed test and an example of using generated API. If an error occurred, you will be notified of a possible delay and the AdHoc support service is dealing with the difficulties encountered.

[Here you can find an example of the generated code](https://github.com/cheblin/AdHoc_LEDBlink_Demo/tree/master/Generated), and here an [example of usage of this code](https://github.com/cheblin/AdHoc_LEDBlink_Demo/tree/master/Examples/STM8) in the above-mentioned demonstration control project with android LED flashes on the demo PCB assembled on STM8.

Using AdHoc, you can easily establish communication not only between microcontrollers, mobile devices but also between the ordinary computers. And what is important, without time and effort waste. In fact, the generated AdHoc code can become the skeleton of your distributed application. The programmer will just have to add handlers to the packet&#39;s reception events, as well as the logic for creating the package, populating it with data and sending it to the recipient.


# AdHoc description file
Basic documentation of the description file format can be found **[here](http://www.unirail.org).** Let's take a look how [**LedBlinkProject** demo description file](https://github.com/cheblin/AdHoc_LEDBlink_Demo) looks like
![descriptionscheme](http://www.unirail.org/wp-content/uploads/2017/12/Capture2.png)

# AdHoc parts relationship scheme

![OverallScheme](http://www.unirail.org/wp-content/uploads/2017/12/OverallScheme.png)

# How to get generated source code

1. Install **Intellij** [IDEA](https://www.jetbrains.com/idea/download/#section=windows) or, if you are planning to deploy your code on Android devices, [Android Studio.](https://developer.android.com/studio/index.html)
2. Download [AdHoc metadata annotations](https://github.com/cheblin/AdHoc/tree/master/org/unirail/AdHoc)
3. Create a new java Project in your IDE and make reference to downloaded metadata annotations. (On **Android Studio** you have to [add JAVA Library module](https://developer.android.com/studio/projects/android-library.html) or edit  [build.gradle](https://github.com/cheblin/AdHoc_LEDBlink_Demo/blob/master/Examples/Android/app/build.gradle) file. Find/add **java.srcDirs** option.)
3. Compose your protocol description file (it should be in UTF8 encoding). **[Rules.](http://www.unirail.org/)**
4. Ensure that description file can me compiled without any errors.
5. Upload your protocol descriptor file with [ClientAgent](https://github.com/cheblin/ClientAgent)
6. In a short time getting your generated, fully tested source code in reply.<br>
In addition it will contain **Demo and Test** file - examples of using generated API and one of the passsed test, respectively.
