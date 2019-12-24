Manual writing of serialization and data deserialization code, especially between heterogeneous units, in different programming languages, 
is a time- consuming process and error-prone. The most efficient solution is to create a DSL – by means of which formally describes the protocol, 
and then create a utility that generates the source code based on this description for various target platforms, in the required programming languages.  

Some examples of solutions built in this way:  
[Protocol Buffers ](https://developers.google.com/protocol-buffers/docs/overview)  
[Cap’n Proto ](https://capnproto.org/language.html)  
[FlatBuffers ](http://google.github.io/flatbuffers/flatbuffers_guide_writing_schema.html)  
[ZCM ](https://github.com/ZeroCM/zcm/blob/master/docs/tutorial.md)  
[MAVLink ](https://github.com/mavlink/mavlink)  
[Thrift](https://thrift.apache.org/docs/idl)

Having studied these, and many other solutions, I have decided to ~~build my own Theme Park with...~~ create **AdHoc protocol**: the system that will implement and complement the merits, 
eliminating the discovered shortcomings.

**AdHoc** protocol is a multilingual **C, C++, Rust, C#, Kotlin, Typescript...Scala/GO(upcoming)**, low-level binary protocol boilerplate code generator.
 According to your protocol description file, **AdHoc** server generates source code, so you just have to insert your received packs handlers, and logic of creating a 
 new package, populating, with data and sending it to receiver.   
**AdHoc** supports:
- bitfields,
- standard set primitive datatypes and more 
- UTF8 strings using everywhere
- `optional` and `requiered` fields
- multidimensional fields with predefined and variable dimensions
- nested packs, enums
- ordinary and flagbits like enums 
- fields with other pack datatype
- fields with enum datatype
- packet level constants
- pack's fields inheritance 
- host's communication interfaces inheritance 
- importing packs and communication interfaces from other protocol descriptions 
- embedded code adapter to simplify fast exchange data between user data objects and **AdHoc** protocol packs 
- [Base 128 Varint](https://developers.google.com/protocol-buffers/docs/encoding) compression.

At the moment, the code generator AdHoc is built like **SaaS**. To get the generated (and tested) code, it is necessary:

-   install **[JDK 8](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)** please
    ensure that `javac` (java compiler) is in PATH and accessible from the console.
-   install any **JAVA** IDE (**[Intellij IDEA](https://www.jetbrains.com/idea/download/)** – just fine)
-   download [AdHoс protocol annotations](https://github.com/cheblin/AdHoc-protocol/tree/master/org/unirail/AdHoc).
    **AdHoc protocol** description projects will need a reference to these
    annotations, precisely to the folder where the `org` - the annotations root
    folder is located. Since annotations are referenced as sources, in **IDEA**, you
    have to add them to the project in this way [Add Content
    Root](https://www.jetbrains.com/help/idea/content-roots.html).
-   To send completed protocol description file to the server (to generate of
    **AdHoc protoco**l handler source code) and receive the result, you will need the
    **[AdHocAgent](https://github.com/cheblin/AdHocAgent)** utility. Please download
    the [precompiled jar](https://github.com/cheblin/AdHocAgent/tree/master/bin)
    or compile the utility from provided source by yourself.

As `DSL` **AdHoc protocol** use java language constructions, in fact protocol description file, is a plain java file. 
So just create **JAVA** project in your favorite IDE and add reference to [AdHoc protocol annotations](https://github.com/cheblin/AdHoc-protocol/tree/master/src/org/unirail/AdHoc)  
Then, create java file in your company namespace and import annotations with **import org.unirail.AdHoc.\*;**. 

Regarding naming in your protocol specification. 
You cannot use names which start/end with `_` underscore. 
Using programming keywords of any programming language that **AdHoc protocol** generator support are prohibited.

**AdHocAgent** is checking used in protocol specification names before upload to server generator.

In the java file, only **one** top-level class can be `public` and it name should be the same as file name. 
In **AdHoc protocol** treat this name as the protocol project name.

# Network topology

Most, similar **AdHoc protocol**, solutions are a concern only on information that nodes exchange.  
**AdHoc protocol** specification provides facilities to describe full network topology: nodes, channels, packs and there relationships.

**None public** file top-level `class` denoted the host/node/device/unit that participate in information exchange. 
 
```java
package org.company.some_namespace;// You project namespace. Required!

import org.unirail.AdHoc.*;//        importing AdHoc protocol annotations 

public class MyDemoProject {//this class ( and file ) name, is the AdHoc protocol description project name
	
	public static class Server implements InCS, InCPP, InC { //this host/node code will be generated in C#, C++ and C
		
	}
	
	public static class Client implements InKT, InTS, InRUST {
		
	}
} 
```
The `implements` java keyword on host, denotes the list of the desired target programming languages for the particular host.

Each host can enclose _several_ communication interfaces through which it exchanges information with others.  
Communication interfaces are expressed with java `interface` keyword.
An interface has to be connected with others by Channels entity. Otherwise, it ignored.

Every communication interface can contain multiple packs, that the host can **RECEIVE** and handle through this interface.   
Packets declarations can be nested in each other.   
Packs are, minimal transmitted information unit, and denoted with `class` java construction inside the interface or host.
Packets declared in the host body, outside of any interfaces, if they are not referenced by other packets situated inside an interface, are ignored. 
> **Packet's names should be unique! This rule is checked by AdHocAgent utility before upload description**  

Every packet should have project scope unique `id` annotation. 
Located in project file packs without `id` annotation, can acquire the `id` automatically from the server on description processing. 
But all packs in imported protocol descriptions should have predefined whole project scope unique `id`.

 
Pack `class` fields are a list of exact information it contains. 

```java
package org.company.some_namespace;

import org.unirail.AdHoc.*;

public class MyDemoProject {
	public static class Server implements InCS, InCPP, InC {
		public interface ToMyClients {
			class Login { //    host Server can receive Login packs through ToMyClients interface
				@__(20) String login;// max 20 bytes array for UTF8 encoded login string
				@__(12) byte   password;//maximum 12 bytes array for password hash
			}
		}
	}
	
	static class Client implements InKT, InTS, InRUST {
		public interface ToServer {
			public class Position { //    host Client can receive Position packs through ToServer interface
				@A long time_usec;//Timestamp (microseconds since system boot or since UNIX epoch)
				float x;//X Position
				float y;//Y Position
				float z;//Z Position
				
				final int  int_constant  = 34; //pack constants
				final byte byte_constant = 2;
			}
		}
	}
}
```
Here, the `Server` host declare, it can **receive** `Login` packet on it's `ToMyClients` interface. 
Every counterpart connected to this interface will get code to create, populate with data, and send the `Login` packet to `Server`.
And the `Client` host declares the capability to receive `Position` packet on its `ToServer` interface.

Packet `class`, internal `static`, `final` fields, **with primitive datatype** (`int_constant`, `byte_constant` ), AdHoc generator treat as `Position` packet constants.

An other protocol description example:
```java
package org.company.some_namespace;

import org.unirail.AdHoc.*;

public class MyDemoProject {
	public static class Server implements InCS, InCPP, InC {
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
	
	public static class Client implements InKT, InTS, InRUST {
		interface ToServer { // Client communication interface
			
			class ServerParams {}
		}
	}
	
	public static class ClientServerLink extends AdvProtocol //channel type
			implements
			Client.ToServer, // connected interfaces
					Server.ToMyClients {}
}
```
On receiving this description, the server generates the following API. (Some minor elements are skipped.)

![image](https://user-images.githubusercontent.com/29354319/70285706-6c599480-1803-11ea-9e0a-d0e4ddd07c9e.png)

`Server` API image was taken from C# generated code.  
`Client` API from the Typescript version.  
It becomes apparent that channel not-connected interfaces are ignored.
The presence `onFirstPack` methods on the `Server`  `ClientServerLink` channel, let Server receive `FirstPack` packet and `sendServerParams` method let it send `ServerParams` pack.

> * **on nodes communication `interfaces`**: using java keyword `extends`  let "inherit" packs declarations from other interfaces. 
> * **on packet `class`** declaration, the keyword  `extends` of some other packet `class`, let to inherit fields from other packet.  
>If the body of the packet `class`, which `extends`, remind empty, if the packet name is :
>  * the same as packet it extends: this not create a new pack, it's just copy extended pack declaration in place. 
>  * is different, then packet it extends: this creates packet alias, the new packet, with a new ID, with copy of all fields from the packet it extends. 

Let see how is it works. Let change specification a bit


```java
package org.company.some_namespace;

import org.unirail.AdHoc.*;

public class MyDemoProject {
	
	public static class Server implements InCS, InCPP, InC {
		public interface ToMyClients extends CommonPacks { // ToMyClients interface inherit all packs from CommonPacks
			class FirstPack {}
		}
		
		public interface IWoker {
			class Job {}
		}
		
		public interface ILogger {
			
			class Log {}
		}
		
		public interface IDispatcher {
			class Dispatch {}
		}
		
		public interface CommonPacks {
			class Pack1 {}
			
			class Pack2 {}
			
			class Pack3 {}
		}
	}
	
	public static class Client implements InKT, InTS, InRUST {
		interface ToServer extends Server.CommonPacks { // getting all packs from Server.CommonPacks
			class ServerParams {}
			
			class Job extends Server.IWoker.Job {} // Job Packet will be inserted in to ToMyClients interface
		}
	}
	
	public static class ClientServerLink extends AdvProtocol //channel type
			implements
			Client.ToServer, // connected interfaces
					Server.ToMyClients {}
}
```

![image](https://user-images.githubusercontent.com/29354319/70288179-1b01d300-180c-11ea-8e09-d679f7f9af6e.png)

All packs: `Pack1`, `Pack2`, `Pack3` from  `CommonPacks` interface now embedded into  `Server.ToMyClients` and `Client.ToServer` interfaces.   
So `Server` and `Client` can send and receive them.

In additional, packet `Job` from `Server.IWoker` interface jumps with the same name into `Client.ToServer` interface, and become `ServerParams` pack neighbor.

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

To join nodes interfaces, **AdHoc protocol** has channels entities.  
Like nodes, they declare at the top-level of description file with java **NONE PUBLIC** `class` construction and consist of three parts.
Channel type, after `extends` keyword, and two connected interfaces after `implements`. Its `class` has an empty body.
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

To express enums (named constants set) **AdHoc protocol** use JAVA enum.
Enums can be declared anywhere even can be nested inside packets declaration.

```java
package org.company.some_namespace;

import org.unirail.AdHoc.*;

public class MyDemoProject {}

enum LIMITS_STATE {
	AQ_NAV_STATUS_INIT, //          assignable fields
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

`@Flags` annotations denote special **Flag Bits enum**.

Not initialized enum fields are automatically assigned integer values. If enum has `@Flags` annotation, generated value, respectively, is **bit flags** like.
If you need more control on enum fields type (*only integer types are supported) and values, use `enum` `static` `final` fields.  

> **Please pay attention:**  enum body cannot be empty, should have at least `;` (semicolon), if you declare only `static` `final` fields. 

# Importing descriptions
Java `import` statement at the beginning of the file before any type definitions but after the package statement, let use interfaces and packages definitions from 
other protocol description files.
Importing definitions should be in full form without the asterisk (*) wildcard character.
```java
import com.company.ProtocolProject;
import com.other_company.OtherProtocolProject;
```
Locations of the importing source files should be provided with `sourcepath` option. 
>**All imported packs should have valid predefined project scope unique `id`.** 
 

# `Optional` and `required` fields

A pack's field can be `optional` or `required`.
 * `required` fields are always allocated and transmitted, even if was not touched and filled with data.
 * `optional` fields, in turns, if was not touched, allocates just a few mark bits


Fields with primitive datatype: 
 * without any annotation are `required`.
 * with annotation which name ends with `_`, (`@A_, @V_, @X_, @I_`) are `optional`
  
Fields with `String`, `dynamic array` and `other packet` data types are always `optional`.


# Multidimensional fields

In AdHoc packet's field can be multidimensional. They are similar to the multidimensional array with constant and variable dimensions length. 
The exact variable dimensions length is set at field initialization.

The following annotations are used to describe multidimensional fields parameters:

|........................................  |  |
|-----|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `@D( dims_params )`  | All space for field items is allocated in advance at field initialization.  Used in a case when the field is most likely to be completely filled with items.|
| `@D_( dims_params )` | Space for items is allocated individually only at items insertion. Used like **sparse** arrays, when the array is most likely be empty. | 

Required field dimensions lengths `dims_params` are pass as annotation parameters, digits separated with `|` or `/`
`Variable dimensions` are denoted with the negative number, which value is the maximum dimension length 

```java
class Server implements InCS, InCPP, InC {
	interface ToMyClients {
		class Pack {
			@D(1 | 2 | 3) short dim_field;      // required multidimensional field of int16_t and fixed 1 x 2 x 3 dimensions 
			@I_() @D(1 | 2 | 3) short dim_field;// optional multidimensional field of int16_t and fixed 1 x 2 x 3 dimensions @I_()- is a special form annotation

			@I_ @D(1 | 2 | 3) short dim_field;  // optional multidimensional field of uint16_t and fixed 1 x 2 x 3 dimensions 

			@A @D(1 | 2 | 3) short dim_field;   // required multidimensional field of uint16_t and fixed 1 x 2 x 3 dimensions 
			@A_ @D(1 | 2 | 3) short dim_field;  // optional multidimensional field of uint16_t and fixed 1 x 2 x 3 dimensions

			@D(1 | -2 | 3) int dim_field;       // optional multidimensional field of int32_t, and fixed first dimension 1  and variable others with max lengths 2 and 3 

			@I @D_(1 | -2 | 7) int dim_field;  // optional multidimensional field of uint32_t and variable second dimension 
		}
	}
}
```

# Array fields

Packet field can array-item: a plain array of primitives. This denoted with array-item `@__( length )` annotation.  
If the annotation parameter `length` is:
-  45 : single number, this is field with fixed item-array length, equal provided value.
- -78 : with `-` mark. this is always `optional` field with variable length item-array. The value is the item-array max length and should be provided at field initialization  
                 If the field is multidimensional, all items will have the same length
- ~32 : with `~` mark. this is always `optional` multidimensional field where each item-array has an individual, variable length. 
        The value is the item-array maximum length and should be provided at each item insertion.


```java
class Server implements InCS, InCPP, InC {
	interface ToMyClients {
		class Pack {
			@__(3)      int field1; // required field of item-array with fixed length of 3 int-s 
			@I_()  @__(3)      int field1; // optional field with item-array fixed length of 3 int-s 
			
            @D(2 | 3) @__(3)      int field1; //required multidimensional field with predefined dimensions 2 x 3 of item-array with fixed length of 3 int-s   
			@D(-3 | 2 | 1) @__(-3) int field2;  // optional multidimensional field with dimensions (up to 3) x 2 x 1 of array-items with all the same, up to 3, length
			@D(1 | 4) @__(~3)     int field3; // optional multidimensional field with predefined dimensions 1 x 4 array-items with an individual, variable, up to 3, length 
		}
	}
}
```

# String fields

Strings in **AdHoc protocol** on all languages are encoded in UTF-8 byte array. By default, without annotation, a string can allocate at most 256 bytes. 
This default length can be changed with array-item `@__( length )` annotation.
All string fields are optional, it means it can be NULL ( does not contain any value).  
String field support only two annotations types:
* `@__( bytes_length )`  - to set maximum string UTF8 encoded bytes
* `D()` and `D_()`  - multidimensional string field   

>other annotations are just silently ignored

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
 

# Numeric fields

As a protocol creator, you know your digits better than any code generator. **AdHoc** provides annotations - the means to share your knowledge with the generator.
That knowledge helps the code generator generate optimal code.

If the numeric field has random values, uniformly distributed in full it's numeric type range, as noise.

![image](https://user-images.githubusercontent.com/29354319/70127303-bdf40900-16b5-11ea-94c9-c0dcd045500f.png)

The compression or encoding of this data type is wasting computation resources.  
For this case exists fields with **only** java numeric datatype. Without annotation the field is signed, `required`and their data treated as normal java number.

![image](https://user-images.githubusercontent.com/29354319/71319106-a3eb6080-24d4-11ea-9f51-10f60e4e0057.png)

Special form `@I_()` annotation, make this field type - `optional`.

Annotation `@I`, **without parameters**, make field unsigned and `reqiered`. With full assigned integer datatype range.
 
![image](https://user-images.githubusercontent.com/29354319/71323707-301b7900-2511-11ea-954c-69aab2a70509.png)

Annotation `@I_` **without parameters**, make field with:
 * integer datatype - unsigned and `optional`,   
 * with a `float` or `double` datatype - `optional`

If some value changes in, for example, range from `200 005` to `200 078`, it could be better, internally, to allocate, to store it, just one-byte with `200 005` constant as offsets.
And provide external, getter/setter functions, that transform value from external to the internal representation and vice versa. 

`@I` and `@I_` annotations can accept these kind parameters in the different forms.
* a single integer value: it's shifting the middle of the range of field assigned integer datatype
```java
     @I_( 31 ) byte field;       //will generate optional field with acceptable values in the range from -96 to 158 with int16_t as external and uint8_t as internal datatype  
 ```
 * range - two integer with `/` or `|` as delimiter. The optimal field's external and internal data type will be calculated by the code generator. Field datatype is ignored
```java
    @I( 0/255 ) long field;            //will generate required field with uint8_t as external and internal datatype
    @I( -2_000 | -1_900 ) short field2; //will generate required field with int16_t as external, uint8_t as internal datatype and -2_000 as shift constant.
```
--------------
If numeric fields have some dispersion/gradient pattern in its value changing...

![image](https://user-images.githubusercontent.com/29354319/70128574-0a404880-16b8-11ea-8a4d-efa8a7358dc1.png)

It is possible to leverage this knowledge to minimize the amount of data transmission.  
In this case code generator can use [Base 128 Varint](https://developers.google.com/protocol-buffers/docs/encoding) compression
algorithm, which allows good and with small resource load, reduces the sending data amount. 
This is achieved by skipping from the transmission of the higher if they are zeros, bytes and then restoring them on the receiving side.

This graph shows the dependence of sending bytes on transferred value.

![image](https://user-images.githubusercontent.com/29354319/70126207-84ba9980-16b3-11ea-9900-48251b545eef.png)

It is becoming clear that with `Base 128 Varint encoding`, smaller value requires fewer bytes to transfer. 

Let highlight three basic types of numeric value changing patterns. 

|.....................pattern...............................|  description 
:-------------------------:|:-------------------------
![image](https://user-images.githubusercontent.com/29354319/70131681-afa9eb00-16bd-11ea-9fcc-c6637d80114c.png)|Rare fluctuations are possible only in the direction of bigger values relative to most probable value `val`. These numeric field is annotated with `@A(val)`
![image](https://user-images.githubusercontent.com/29354319/70130976-7d4bbe00-16bc-11ea-946a-cfa533a09efd.png)|Rare fluctuations are possible in both directions relative to most probable value `val`. These numeric field is annotated with`@X(val)`
![image](https://user-images.githubusercontent.com/29354319/70131118-bbe17880-16bc-11ea-84a2-2a2a4106a810.png)|Fluctuations are possible only in the direction of smaller values relative to most probable value `val`.These numeric field is annotated with  `@V(val)`


The most probable value  – **val** is passed as annotation argument. This value can be a single number @V(-11 ) or it can be pass as range: two number separated by `/` @A(-11 / 75)

 `@A, @V, @X, @I` annotations with _ `@A_, @V_, @X_, @I_` make numeric field `optional` 

### Using example
```java
class Server implements InCS, InCPP, InC {
	interface ToMyClients {
		class Pack {
			             byte  field;    //required field, the field data before sending is not encoded (poorly compressible), and can take values in the range from -128 to 127                      
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

# Field with Other packet datatype

As enums can be any field's data type, a packet can be, field's data type to. Packets enclosing can be as deep as it needed. Cycle enclosing is prohibited.

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
	interface ToServer { // Client communication interface
		
		class ServerParams {
			@D(-5) Geo.Point position; //multidimensional field of Geo.Point items with one dimension up to 5 items
			String name;
			@A long id;
		}
	}
	
	interface Geo {
		class Point {
			float x;
			float y;
			float z;
		}
	}
}

class ClientServerLink extends AdvProtocol //channel type
		implements
		Client.ToServer, // connected interfaces
				Server.ToMyClients {}
```
