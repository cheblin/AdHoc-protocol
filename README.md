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

Having studied these, and many other solutions, I have decided to ~~build my own Theme Park with...~~ create a system that will implement and complement the merits, 
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

At the moment, the code generator AdHoc is built like **SaaS**. To get the generated and tested code it is necessary:

-   install **[JDK 8](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)** please
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

As `DSL` AdHoc protocol use java language constructions, in fact protocol description file, is a plain java file. 
So just create **JAVA** project in your favorite IDE and add reference to [AdHoc protocol annotations](https://github.com/cheblin/AdHoc-protocol/tree/master/src/org/unirail/AdHoc)  
Create java file in your company namespace and import annotations with **import org.unirail.AdHoc.\*;**. 

Regarding naming in your protocol specification. You cannot use names which start/end with `_` underscore. 
Using programming keywords of any programming language that AdHoc protocol generator support are prohibited.
**AdHocAgent** is checking used in protocol specification names before upload to server generator.

In the java file, only **one** top-level class can be `public` and it name should be the same as file name. 
In AdHoc protocol treat this name as the protocol project name.

# Network topology

Most, similar AdHoc protocol, solutions are a concern only on information that nodes exchange.
AdHoc protocol specification requires to describe full network topology: nodes, channels and packs.

**None public** file top-level `class` denoted the host/node/device/unit that participate in information exchange. 
 
```java
package org.company.some_namespace;// You project namespace. Required!

import org.unirail.AdHoc.*;//        importing AdHoc protocol annotations 

public class MyDemoProject {} //this public class name is AdHoc protocol project name

class Server implements InCS, InCPP, InC { //for this host code will be generated in C#, C++ and C

}

class Client implements InKT, InTS, InRUST {
	
}
```
The `implements` java keyword denotes the list of the desired target programming languages for the particular host.

Each participant can have _several_ interfaces thru which they exchange the information with others. Interfaces are expressed with java `interface` keyword.

Every interface can contain multiple packs that host can **RECEIVE** and handle through this interface.
Packs are, minimal transmitted information unit, and denoted with `class` java construction inside the interface. 
> **Packets names should be unique!**  
 
Pack `class` fields are a list of exact information it contains. 

```java
package org.company.some_namespace;

import org.unirail.AdHoc.*;

public class MyDemoProject {}

class Server implements InCS, InCPP, InC {
	interface ToMyClients {
		class Login { //    host Server can receive Login packs through ToMyClients interface
			@__(20) String login;// max 20 bytes array for UTF8 encoded login string
			@__(12) byte   password;//maximum 12 bytes array for password hash
		}
	}
}

class Client implements InKT, InTS, InRUST {
	interface ToServer {
		class Position { //    host Client can receive Position packs through ToServer interface
			@A long time_usec;//Timestamp (microseconds since system boot or since UNIX epoch)
			float x;//X Position
			float y;//Y Position
			float z;//Z Position
			
			final int  int_constant  = 34; //pack constants
			final byte byte_constant = 2;
		}
	}
}
```
Packet `class`, internal `static`, `final` fields **with primitive datatype**, AdHoc generator treat as packet constants.

  

For example a protocol description
```java
package org.company.some_namespace;

import org.unirail.AdHoc.*;

public class MyDemoProject {}

class Server implements InCS, InCPP, InC {
	interface ToMyClients { // node interface
		class FirstPack {}
	}
	
	interface IWoker {
		class Job {}// Job Packet
	}
	
	interface ILogger {
		
		class Log {}
	}
	
	interface IDispatcher {
		class Dispatch {}
	}
	
	interface CommonPacks {
		class Pack1 {}
		
		class Pack2 {}
		
		class Pack3 {}
	}
}

class Client implements InKT, InTS, InRUST {
	interface ToServer { // Client communication interface
		
		class ServerParams {}
	}
}

class ClientServerLink extends AdvProtocol //channel type
		implements
		Client.ToServer, // connected interfaces
				Server.ToMyClients {}

```
On receiving this description, the server generates the following API. (Some minor elements are skipped.)


![image](https://user-images.githubusercontent.com/29354319/70285706-6c599480-1803-11ea-9e0a-d0e4ddd07c9e.png)

`Server` API image was taken from C# generated code.  
`Client` API from the Typescript version.  
It becomes apparent that channel not-connected interfaces are ignored.
The presence `onFirstPack` methods on the `Server`  `ClientServerLink` channel, let Server receive `FirstPack` packet and `sendServerParams` method let it send `ServerParams` pack.

> * **on nodes communications `interfaces`**:  java keyword `extends`  let "inherit" packs declarations from other interfaces. 
> * **after packet `class`** declaration, the keyword : 
>  * `implements` of communication interfaces, let this packet be embedded into "implemented" interfaces. 
>  * `extends` of some other packet `class`, let to inherit fields from other packet

Let see how is it works. Let change specification a bit



```java
package org.company.some_namespace;

import org.unirail.AdHoc.*;

public class MyDemoProject {}

class Server implements InCS, InCPP, InC {
	interface ToMyClients extends CommonPacks { // ToMyClients interface inherit all packs from CommonPacks
		class FirstPack {}
	}
	
	interface IWoker {
		class Job implements ToMyClients {}  // Job Packet will be inserted in to ToMyClients interface
	}
	
	interface ILogger {
		
		class Log {}
	}
	
	interface IDispatcher {
		class Dispatch {}
	}
	
	interface CommonPacks {
		class Pack1 {}
		
		class Pack2 {}
		
		class Pack3 {}
	}
}

class Client implements InKT, InTS, InRUST {
	interface ToServer extends Server.CommonPacks { // Client communication interface
		
		class ServerParams {}
	}
}

class ClientServerLink extends AdvProtocol //channel type
		implements
		Client.ToServer, // connected interfaces
				Server.ToMyClients {}
```



![image](https://user-images.githubusercontent.com/29354319/70288179-1b01d300-180c-11ea-8e09-d679f7f9af6e.png)

All packs: `Pack1`, `Pack2`, `Pack3` from  `CommonPacks` interface now embedded into  `Server.ToMyClients` and `Client.ToServer` interfaces.   
So `Server` and `Client` can send and receive them.

In additional, packet `Job` from `IWoker` interface jumps into `Server.ToMyClients` interface, and become `FirstPack` neighbor.

If we take a look at our protocol description now it's changed by the server.
```java
package org.company.some_namespace;

import org.unirail.AdHoc.*;

public class MyDemoProject {}

class Server implements InCS, InCPP, InC {
	interface ToMyClients extends CommonPacks { // node interface
	@id(0)
	class FirstPack {}
	}
	
	interface IWoker {
	@id(1)
	class Job implements ToMyClients {}  // Job Packet
	}
	
	interface ILogger {// Server communication interface
		
	@id(2)
	class Log {}
	}
	
	interface IDispatcher {
	@id(3)
	class Dispatch {}
	}
	
	interface CommonPacks {
	@id(4)
	class Pack1 {}
		
	@id(5)
	class Pack2 {}
		
	@id(6)
	class Pack3 {}
	}
}

class Client implements InKT, InTS, InRUST {
	interface ToServer extends Server.CommonPacks { // Client communication interface
		
	@id(7)
	class ServerParams {}
	}
}

class ClientServerLink extends AdvProtocol //channel type
		implements
		Client.ToServer, // connected interfaces
				Server.ToMyClients {}
```

 The server assigns `id` annotation with a unique number to every packet that has not. Keep this `Id` if you concern backward compatibility


# Channels

To join nodes interfaces, AdHoc protocol has channels entities.  
Like nodes, they declare at the top-level of description file with java `class` construction and consist of three parts.
Channel type after `extends` keywords and two connected interfaces after `implements`.
```java
package org.company.some_namespace;

import org.unirail.AdHoc.*;

public class MyDemoProject {}

class Server implements InCS, InCPP, InC {
	interface ToMyClients { // node interface
		class FirstPack {}
	}
}

class Client implements InKT, InTS, InRUST {
	interface ToServer {} // node interface 
}

class ClientServerLink extends AdvProtocol //channel type
		implements
		Client.ToServer, // connected interfaces
				Server.ToMyClients {}
```

To transmit data via the radio channel / raw UART, use `AdvChannel` type entity. 
It builtin [byte stuffing framing](https://web.cs.wpi.edu/~rek/Undergrad_Nets/B07/BitByteStuff.pdf) to fast recover after channel failure and CRC.  
If data are sending over secure transport or if AdHoc used as a serialization tool of the program data in a file, the `StdChannel` type is using.

# Enums

To express enums (named constants set) AdHoc protocol use JAVA enum `static`, `final` fields.
Enums declare at the file top-level, next to nodes classes.

```java
package org.company.some_namespace;

import org.unirail.AdHoc.*;

public class MyDemoProject {}

enum LIMITS_STATE {
	AQ_NAV_STATUS_INIT,
	AQ_NAV_STATUS_STANDBY,
	AQ_NAV_STATUS_MANUAL,
	AQ_NAV_STATUS_ALTHOLD,
	AQ_NAV_STATUS_POSHOLD,
	AQ_NAV_STATUS_GUIDED;
	
	final int
			LIMITS_INIT       = 0, //pre-initialization
			LIMITS_DISABLED   = 1, //disabled
			LIMITS_ENABLED    = 2, //checking limits
			LIMITS_TRIGGERED  = 3, //a limit has been breached
			LIMITS_RECOVERING = 4, //taking action eg. RTL
			LIMITS_RECOVERED  = 5;  //we're no longer in breach of a limit
}

@Flags enum LIMIT_MODULE {
	;
	
	final int
			LIMIT_GPSLOCK  = 1, //pre-initialization
			LIMIT_GEOFENCE = 2, //disabled
			LIMIT_ALTITUDE = 4;  //checking limits
}

class Client implements InKT, InTS, InRUST {
	interface ToServer {
		class Position {
			LIMIT_MODULE limit_module;
			LIMITS_STATE limits_state;
		}
	}
}
```
`@Flags` annotations denote special Flag Bits enum.

Not initialized enum fields are automatically assigned integer values. If enum has `@Flags` annotation, generated value respectively: is bit flags like.
If you need full control on enum fields type (*only integer types are supported) and values, use `static` `final` fields.  

> **Please pay attention:** if enum has only static fields, its body cannot be empty, should have at least ; (semicolon)



The pack fields annotations provide additional meta-information for the code generator.

# `Optional` and `required` fields

Any field in **AdHoc** protocol can be `required` or `optional`. 
Without the special annotation, all fields with primitive datatype are `required`   
`required` field allocates space in the transmitted packet even if it was not filled with data.  
in its turn empty `optional` field allocates in packet just a few bits.  
 Fields with String and nested packet datatype are optional.
Any field with primitive or array-item datatype can be made `optional` with a special form of annotation that ends with _ (underscore) `@A_, @V_, @X_, @I_`  
Before read data from the `optional` field, we need to ensure that it contains any data. 


# Multidimensional fields

AdHoc protocol generator recognizes multidimensional fields. They work exactly like a multidimensional array. 
Some dimensions can have a variable length. Exact length determined at field initialization.

The following annotations are used to describe multidimensional fields

|........................................  |  |
|-----|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `@D( dims_params )`  | All space for field items is allocated in advance at field initialization.  Used in a case when the field is most likely to be completely filled with items.|
| `@D_( dims_params )` | Space for items is allocated only when actually item inserted. Used for **sparse** arrays, when the array is most likely to be poorly filled. | 

Dimensions lengths are pass as annotation parameters and  separated with `|` 
Variable dimensions are denoted with the negative number which value is the maximum dimension length 

```java
class Server implements InCS, InCPP, InC {
	interface ToMyClients {
		class Pack {
			@D_(1 | 2 | 7) short dim_field;// field of shorts with 1 x 2 x 7 dimensions 
			@D(2 | -7) int var_dim_field;//the last dimension has variable, up to 7 length 
		}
	}
}
```

# Fields with array-items

Packet field can store array-item: a plain array of primitives. This denoted with array-item `@__( length )` annotation.  
If the annotation parameter `length` is:
-  45 - positive number, this number is the length of the array of the item.
- -78 - negative. The number is the maximum array variable length. All field array-items have the same length. The exact length is determined at field initialization.
- ~32 - with ~ . The number is the maximum array variable length. Each field array-item have individual variable length, fit to inserted data

### Using example

```java
class Server implements InCS, InCPP, InC {
	interface ToMyClients {
		class Pack {
			@D(2 | 3) @__(3)      int field1; // Multidimensional field with predefined dimensions 2 x 3  
			// Returns  array-items of predefined length of 3 ints.
			@D(-3 | 2 | 1) @__(-3) int field2;  // A multidimensional field with dimensions (up to 3) x 2 x 1   
			// Returns array-items with same up to 3, length
			@D(1 | 4) @__(~3)     int field3; // Multidimensional field with predefined dimensions 1 x 4
			// Returns array-items with individual variable up to 3, length 
		}
	}
}
```

# String fields

Strings in AdHoc protocol on all languages are encoded in UTF-8 byte array. By default, without annotation, a string can allocate at most 256 bytes. 
This default length can be changed with array-item `@__( length )` annotation.
All string fields are optional, it means it can be NULL ( does not contain any value).

### Using example

```java
class Server implements InCS, InCPP, InC {
	interface ToMyClients {
		class Pack {
			String  at_most_256_bytes_string; // field hold string with default, at most 265 bytes length 
			@__( 1024 ) String at_most_1024_bytes_string; // field with string that syze at most 1024 bytes length 
		}
	}
}
```
 

# Numeric fields value changing dispersion

The packet numeric fields can be annotated with `@A, @V, @X, @I` This annotations are denoting the
meta-information about the pattern of the field value changing. Based on this
information, code generator can skip or apply [Base 128 Varint](https://developers.google.com/protocol-buffers/docs/encoding)  compression
algorithm, which allows good and with small resource load, reduces the sending data amount. 
This is achieved by skipping transmission of the higher, if they are zeros, bytes and then restoring them on the receiving side.

This graph shows the dependence of sending bytes on transferred value.

![image](https://user-images.githubusercontent.com/29354319/70126207-84ba9980-16b3-11ea-9900-48251b545eef.png)

It is became clear that with `Base 128 Varint encoding`, smaller value require less bytes to transfer.
As protocol creator, you know your numbers better than any generator, and annotations are the means by which you share your knowledge with the generator.

For example, if numeric field have random values, uniformly distributed in full range. Almost as noise.

![image](https://user-images.githubusercontent.com/29354319/70127303-bdf40900-16b5-11ea-94c9-c0dcd045500f.png)

The compression or encoding of this type of data is wasting computation resources. This kind of data is better to transmit as-is.
This type numeric fields should not have any annotation or have `@I` if needed.

Otherwise, if numeric fields have some dispersion/gradient pattern in its value changing...

![image](https://user-images.githubusercontent.com/29354319/70128574-0a404880-16b8-11ea-8a4d-efa8a7358dc1.png)

It is possible to leverage this knowledge to minimize the amount of data transmission.

#### Let highlight three basic types of numeric value changing patterns. 

|.....................pattern...............................|  description 
:-------------------------:|:-------------------------
![image](https://user-images.githubusercontent.com/29354319/70131681-afa9eb00-16bd-11ea-9fcc-c6637d80114c.png)|Rare fluctuations are possible only in the direction of bigger values relative to most probable value `val`. These numeric field is annotated with `@A(val)`
![image](https://user-images.githubusercontent.com/29354319/70130976-7d4bbe00-16bc-11ea-946a-cfa533a09efd.png)|Rare fluctuations are possible in both directions relative to most probable value `val`. These numeric field is annotated with`@X(val)`
![image](https://user-images.githubusercontent.com/29354319/70131118-bbe17880-16bc-11ea-84a2-2a2a4106a810.png)|Fluctuations are possible only in the direction of smaller values relative to most probable value `val`.These numeric field is annotated with  `@V(val)`


The most probable value  – **val** is passed as annotation argument. this value can be a number @V(-11 ) or it can be pass as range: two number separated by `/` @A(-11 / 75)

 `@A, @V, @X, @I` annotations with _ `@A_, @V_, @X_, @I_` make numeric field `optional` 

### Using example
```java
class Server implements InCS, InCPP, InC {
	interface ToMyClients {
		class Pack {
			@I           byte  field;    //required field, the field data before sending is not encoded (poorly compressible), and can take values in the range from -128 to 127                      
			@A           byte  field1;   //required field, the data is compressed, the field can take values in the range from 0 to 255. In fact it is an analogy to the type uint8_t in C. 
			@I(-1000)    byte  field2;   //required field, (not to be compressed), the field can take values in the range from -1128 to -873                                                          
			@X_          short field3;   //optional field takes values in the range  from -32 768 to 32 767. will be compressed with ZigZag on sending.                                       
			@A(1000)     short field4;   //required field takes a value between – 1 000 to 65 535 will be compressed on sending.                                                           
			@V_          short field5;   //optional field takes a value between  -65 535  to 0  will be compressed on sending.                                                                 
			@I(-11 / 75) short field6;   //required field with uniformly distributed values in the specified range.     
		}
	}
}                                                                                    
```

# Bits fields

In some cases is critical to transmitting as little bytes as possible. `@B( bits )` annotation denote how many bits will field allocate.
With `@B( from / to )` form let you set acceptable numbers range and code generator estimate bits amount.  

# Nested packets

As enums can be any field datatype, any packet can become, field datatype. Packets enclosing can be as deep as it needed. Cycle enclosing is prohibited.