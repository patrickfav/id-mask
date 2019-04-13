# IDMask - Encryption and Obfuscation of IDs

IDMask is a library for masking **internal ids** (e.g. from your DB) when they need to be publicly published to **hide their actual value and to prevent forging**. This should make it very hard for an attacker to understand provided ids (e.g. by witnessing a sequence, deducting how many order you had, etc.) and **prevent guessing** of possible valid IDs. Masking is **fully reversible** and also supports optional **randomization** for e.g. **shareable links** or **one-time tokens**. It has a wide support for various **Java types** including `long`, `UUID` and `BigInteger`. This library bases its security on **strong cryptographic primitives** ([AES](https://en.wikipedia.org/wiki/Advanced_Encryption_Standard), [HMAC](https://en.wikipedia.org/wiki/HMAC), [HKDF](https://en.wikipedia.org/wiki/HKDF)) to create a secure encryption schema. It was inspired by [HashIds](https://hashids.org/) but tries to tackle most of it's shortcomings.


[![Download](https://api.bintray.com/packages/patrickfav/maven/id-mask/images/download.svg)](https://bintray.com/patrickfav/maven/id-mask/_latestVersion)
[![Build Status](https://travis-ci.org/patrickfav/id-mask.svg?branch=master)](https://travis-ci.org/patrickfav/id-mask)
[![Javadocs](https://www.javadoc.io/badge/at.favre.lib/id-mask.svg)](https://www.javadoc.io/doc/at.favre.lib/id-mask)
[![Coverage Status](https://coveralls.io/repos/github/patrickfav/id-mask/badge.svg?branch=master)](https://coveralls.io/github/patrickfav/id-mask?branch=master) [![Maintainability](https://api.codeclimate.com/v1/badges/fc50d911e4146a570d4e/maintainability)](https://codeclimate.com/github/patrickfav/id-mask/maintainability)

## Feature Overview

* **Secure**: Creates encrypted IDs with **no-nonsense cryptography** ([AES](https://en.wikipedia.org/wiki/Advanced_Encryption_Standard), [HKDF](https://en.wikipedia.org/wiki/HKDF)) including **forgery protection** ([HMAC](https://en.wikipedia.org/wiki/HMAC))
* **Wide range of Java type support**: mask ids from `long`, `UUID`, `BigInteger`, `LongTuple` and `byte[]`
* **Full support of types**: no arbitrary restrictions like "only positive longs", etc.
* **ID randomization**: if enabled, will create IDs which appear uncorrelated with the same underlying value.
* **No collisions possible**: because the IDs are not hashed or otherwise compressed, collisions are impossible
* **Built-in caching support**: To increase performance a simple caching framework can be facilitated.
* **Lightweight & Easy-to-use**: the library has only minimal dependencies and a straight forward API
* **Fast**: 8 byte ids take about `2µs` and 16 byte ids `7µs` to mask on a fast desktop machine (see JMH benchmark)
* **Supports multiple encodings**: Depending on your requirement (short IDs vs. readability vs. should not contain words) multiple encodings are available including [Base64](https://en.wikipedia.org/wiki/Base64), [Base32](https://en.wikipedia.org/wiki/Base32) and [Hex](https://en.wikipedia.org/wiki/Hexadecimal) with the option of providing a custom one.

The code is compiled with target [Java 7](https://en.wikipedia.org/wiki/Java_version_history#Java_SE_7) to keep backwards compatibility with *Android* and older *Java* applications.

## Quickstart

Add the dependency to your `pom.xml` ([check latest release](https://github.com/patrickfav/id-mask/releases)):

    <dependency>
        <groupId>at.favre.lib</groupId>
        <artifactId>id-mask</artifactId>
        <version>{latest-version}</version>
    </dependency>

A very simple example using 64 bit integers ([`long`](https://docs.oracle.com/javase/7/docs/api/java/lang/Long.html)):

```java
byte[] key = Bytes.random(16).array();
long id = ...

IdMask<Long> idMask = IdMasks.forLongIds(Config.builder(key).build());

String maskedId = idMask.mask(id);
//example: NPSBolhMyabUBdTyanrbqT8
long originalId = idMask.unmask(maskedId);
```

alternatively using [`UUIDs`](https://docs.oracle.com/javase/7/docs/api/java/util/UUID.html):

```java
UUID id = UUID.fromString("eb1c6999-5fc1-4d5f-b98a-792949c38c45");

IdMask<UUID> idMask = IdMasks.forUuids(Config.builder(key).build());

String maskedId = idMask.mask(id);
//example: rK0wpnG1lwvG0xiZn5swxOYmAvxhA4A7yg
UUID originalId = idMask.unmask(maskedId);
```

Examples for other java types (e.g. [`BigInteger`](https://docs.oracle.com/javase/7/docs/api/java/math/BigInteger.html), [`byte[]`](https://docs.oracle.com/javase/7/docs/api/java/lang/Byte.html) and `LongTuple`), see below.

## How-To

The following section explains in detail how to use and configure IDMask:

* **Step 1:** How to create your secret key
* **Step 2:** Select the Java type to use
* **Step 3:** Adjust the configuration to your needs

### Step 1: Create a Secret Key

IDMask's security relies on the strength of the used cryptographic key. In it's rawest from, a secret key is basically just a random byte array. A provided key should be at least 16 bytes long (longer usually doesn't translate to better security). IDMask requires it to be between 12 and 64. There are multiple ways to manage secret keys, if your project already has a managed [`KeyStore`](https://docs.oracle.com/javase/7/docs/api/java/security/KeyStore.html) or similar, use it. Otherwise you could just hardcode the key in your code. This, of course, only makes sense where the client doesn't have access to the source or binary (i.e. in a backend scenario). Here are some suggestion on how to create your secret key:

#### Option A: Use Random Number Generator CLI

One of the easiest ways to create a high quality key is to use this java cli: [Dice](https://github.com/patrickfav/dice/releases). Just download the `.jar` (or `.exe`) and run:

    java -jar dice.jar 16 -e "java"

This will generate multiple 16 byte long syntactically correct java byte arrays:

    new byte[]{(byte) 0xE4, (byte) 0x8A, ...};

You could just hard code this value:

    private static final byte[] ID_MASK_KEY = new byte[]{(byte) 0xE4, (byte) 0x8A, ...};
    
#### Option B: Generate a Random Key within a Java Runtime

Either in the [debugger](https://www.jetbrains.com/help/idea/debugging-your-first-java-application.html), simple application or any other [REPL](https://en.wikipedia.org/wiki/Read%E2%80%93eval%E2%80%93print_loop) execute the following code (IDMask must be in classpath):

    Bytes.random(16).encodeHex();

which will create a random byte array using `SecureRandom` and encodes it as [hex string](https://en.wikipedia.org/wiki/Hexadecimal). In your code use this hex parser and the previously generated string:

    private static final byte[] ID_MASK_KEY = Bytes.parseHex("e48a....").array();

Either way, don't worry too much as the library supports changing the secret key while still supporting unmasking of older ids.

### Step 2: Choosing the correct Type

IDMask basically supports 2 data types:

* 64 bit long words (8 byte)
* 128 bit long words  (16 byte)

Data types with these byte lengths can be represented as various Java types often used as identifiers:

#### Option A: 64-bit integers (long)

The most common case and the only one fitting in the '8 byte' category is an id with the type [`long`](https://docs.oracle.com/javase/7/docs/api/java/lang/Long.html). 
In Java a `long` is a signed integer and can represent `-2^63` to `2^63 -1`. IDMask can mask any valid `long` value.
Internally it will be represented as 8 byte, two's complement.

Create a new instance by calling:

```java
IdMask<Long> idMask = IdMasks.forLongIds(Config.builder(key).build());
String masked = idMask.mask(-588461182736122L);
```

It is of course possible to also pass `int` types by casting them:

```java
String masked = idMask.mask((long) 1780);
```

#### Option B: Universally unique identifier (UUIDs)

A [UUID](https://en.wikipedia.org/wiki/Universally_unique_identifier) is a 128 bit long identifier (with 122 bit entropy) and
often used in databases because one does not have to worry about sequences or duplicates, but can just generate
unique ids but choosing one randomly. Java has first level support for [UUIDs](https://docs.oracle.com/javase/7/docs/api/java/util/UUID.html) for generation, parsing and serialization.

Create a new instance by calling:

```java
IdMask<UUID> idMask = IdMasks.forUuids(Config.builder(key).build());
String masked = idMask.mask(UUID.fromString("eb1c6999-5fc1-4d5f-b98a-792949c38c45"));
```

#### Option C: Arbitrary-Precision Integers (BigInteger)

If your ids are typed as [BigInteger](https://docs.oracle.com/javase/7/docs/api/java/math/BigInteger.html) you can either convert them to long (if they are bound to 64 bit) or use the specific IDMask implementation. Note that the big integer will be converted to a [two's complement](https://en.wikipedia.org/wiki/Two%27s_complement) byte representation and are supported up to 15 byte length (i.e. up to `2^120`).

```java
IdMask<BigInteger> idMask = IdMasks.forBigInteger(Config.builder(key).build());
String masked = idMask.mask(BigInteger.ONE);
```

#### Option D: Tuple of 64-bit integers

Sometimes it makes sense to encode multiple ids to a single serialized version. Use this if you want to combine two `long` ids, making it even harder to understand individual ones within.

```java
IdMask<LongTuple> idMask = IdMasks.forLongTuples(Config.builder(key).build());
String masked = idMask.mask(new LongTuple(182736128L, 33516718189976L));
```
#### Option E: 16 byte (128 bit) byte array

**Only for advanced use cases.** The most generic way to represent a 128 bit id is as a byte array. Basically you may provide any data as long as it fits in 16 bytes. *Note, that this is not a general purpose encryption schema and your data might not be secure!* 

```java
IdMask<byte[]> idMask = IdMasks.for128bitNumbers(Config.builder(key).build());
String masked = idMask.mask(new byte[] {0xE3, ....});
```

#### Option F: Encoding multiple 32-bit integers

Not supported directly, but still quite easy to implement - if you use only 4 byte (32-bit) integers (`int`), you can encoded multiple numbers. 

Using the `long` schema you can encode up to two of those:

```java
int intId1 = 1;
int intId2 = 2;

IdMask<Long> idMask = IdMasks.forLongIds(Config.builder(key).build());

long encodedInts = Bytes.from(intId1, intId2).toLong();
String masked = idMask.mask(encodedInts);
long raw = idMask.unmask(masked);
int[] originalIds = Bytes.from(raw).toIntArray(); // originalIds[0] == intId1; originalIds[1] == intId2
```

or using four 32-bit integers with the `byte[]` schema:

```java
int intId1 = 1;
int intId2 = 2;
int intId3 = 3;
int intId4 = 4;

IdMask<byte[]> idMask = IdMasks.for128bitNumbers(Config.builder(key).build());

byte[] ids = Bytes.from(intId1, intId2, intId3, intId4).array();
String masked = idMask.mask(ids);
byte[] raw = idMask.unmask(masked);
int[] originalIds = Bytes.from(raw).toIntArray(); // originalIds[0] == intId1; originalIds[1] == intId2,...
```

#### Option G: Strings?

Per design this library lacks the feature to mask string based ids. This is to discourage using it as general purpose encryption library. In most cases strings are encoded data: e.g. `UUIDs` string representation, `hex`, `base64`, etc. Best practice would be to decode these to a byte array (or `UUID` if possible) and use any of the other options provided above. (Note: *technically* it would be possible to convert the string to e.g. [ASCII](https://en.wikipedia.org/wiki/ASCII) bytes and just feed it `IdMask<byte[]>` if it's length is equal or under 16; **but this is highly discouraged**).

### Step 3: IDMask Configuration

Usually the default settings are fine for most use cases, however it may make sense to adapt to different usage scenarios with the following settings.

#### Q1: Should Ids be deterministic or random?

By default off, the masking algorithm supports randomization of generated ids. This is achieved by creating a random number and using it as part of the encrypt scheme as well as appending it to the output of the masked id. Therefore randomized Ids are longer than their deterministic counter part. Randomization increases the obfuscation effectiveness but makes it impossible for a client to check equality. This usually makes sense with shareable links, random access tokens, or other one-time identifiers. Randomized ids within models are probably a bad idea. 

For instance these masked ids all represent the same original id `70366123987523049`:

```
SUkHScdj3j9sE3B3K8KGzgc
Hx8KpcbNQb7MAAnKPW-H3D4
wsKW652TCEDBjim8JfOmLbg
```

Enable with:

```java
Config.builder(key)
    .randomizedIds(true)
    ...
```        

#### Q2: What encoding should I choose?

The library internally converts everything to bytes, encrypts it and then requires an encoding schema to make the output printable. Per default the url-safe version of Base64 ([RFC4648](https://tools.ietf.org/html/rfc4648)) is used. This is a well supported, fast and reasonable space efficient (needs ~25% more storage than the raw bytes) encoding. Note that the output size is constant using the same settings a type and does _not_ grow or shrink depending on e.g. how big the number is.

However depending on your use case, you may want Ids that are easy to type, do not contain possible problematic words
or require some maximum length. The library includes some built-in encodings which satisfy different requirements:


| Encoding               | may contain words | easy to type                       | url safe | Length for 64 bit id (deterministic/randomized) | Length for 128 bit id (deterministic/randomized) | Example                              |
|------------------------|-------------------|------------------------------------|----------|-------------------------------------------------|--------------------------------------------------|--------------------------------------|
| Hex                    | no                | yes                                | yes      | 34/50                                           | 50/82                                            | `e5e53e09bbd37f8d8b9afdfbed776de6fe` |
| Base32                 | yes               | yes                  | yes      | 28/40                                           | 40/66                                            | `XS6GLNDNQ2NSBWJRMWM3U72FTLLA`       |
| Base32 (Safe Alphabet) | no curse words    | contains upper and lowercase       | yes      | 28/40                                           | 40/66                                            | `pVY2YYbV8GyzaEZ3aB5b87EeP4Da`       |
| Base64                 | yes               | no                                 | yes      | 23/34                                           | 34/55                                            | `SkqktDj1MVEkiPMrwg1blfA`            |

If ids should be as short as possible, you may look into using [Ascii85/Base85](https://en.wikipedia.org/wiki/Ascii85) with a Java implementation [here](https://github.com/fzakaria/ascii85); expect around 8% better space efficiency compared to Base64. 

Choose a different encoding by setting the following in the config builder:

```java
Config.builder(key)
    .encoding(new ByteToTextEncoding.Base32Rfc4648())
    ...
```
Implement your own encoding by using the `ByteToTextEncoding` interface.

#### Formatted IDs

For IDs that are better readable for humans you can use the `ByteToTextEncoding.Formatter` and use it to wrap any other encoding instance with: 

    ByteToTextEncoding.Formatter.wrap(myEncoding);

For example with Base32 this could look like this

    SH4RT-7LNHU7X-X3TMJ-OJYNMDS-ETVQ

#### Q3: Do you need Caching?

 By default a simple in-memory [lru cache](https://en.wikipedia.org/wiki/Cache_replacement_policies#Least_recently_used_(LRU)) is enabled. This cache improves performance if recurring ids are encoded/decoded - if this is not the case the cache should be disabled to safe memory.

This setting is responsible for disabling the cache:

```java
Config.builder(key)
    .enableCache(true)
    ...
```

if you want to wire your own cache framework to the id mask library you may do so by implementing the `Cache` interface and setting:

```java
Config.builder(key)
    .cacheImpl(new MyHazelcastCache())
    ...
```

#### Q4: Any other Advanced Security Features required?

You may provide your own [JCA provider](https://docs.oracle.com/javase/7/docs/technotes/guides/security/crypto/CryptoSpec.html) (like [BouncyCastle](https://www.bouncycastle.org/)) or your own cryptographically secure pseudorandom number generator
(i.e. a [SecureRandom](https://docs.oracle.com/javase/8/docs/api/java/security/SecureRandom.html) implementation). The provider is used to encrypt/decrypt with [AES](https://en.wikipedia.org/wiki/Advanced_Encryption_Standard) and to calculate [HMACs](https://en.wikipedia.org/wiki/HMAC)

Example:
```java
Config.builder(key)
    .secureRandom(new SecureRandom())
    .securityProvider(Security.getProvider("BC"))
    ...
```

#### High Security Mode

Only applicable with 16 byte ids (e.g. `UUID`, `byte[]`, `BigInteger`, ...) it is optionally possible to increase the security
strength of the masked id in exchange for increased id lengths. By default a 8-byte [MAC](https://en.wikipedia.org/wiki/Message_authentication_code)
 is appended to the ID and, if randomization is enabled, a 8-byte random nonce is prepended. In high security mode these 
 numbers double to 16 byte, therefore high security IDs are 16 bytes longer. If you generate a massive amount of ids or don't 
 mind the longer output length, high security mode is recommended.

Issue with smaller MAC is increased chance of not recognizing a forgery and issue with smaller randomization nonce is higher
chance of finding duplicated randomization values and recognizing equal ids (chance of duplicate after 5,000,000,000 randomized ids
with 8 byte nonce is 50%). Increasing these numbers to 16 bytes make both those issue negligible.

### A Full Example

Here is a fully wired example using the generic byte array IDMask:

```java
IdMask<byte[]> idMask = IdMasks.for128bitNumbers(
        Config.builder(KeyManager.Factory.with(key))
                .randomizedIds(true) //non-deterministic output
                .enableCache(true)
                .cacheImpl(new Cache.SimpleLruMemCache(64))
                .encoding(new ByteToTextEncoding.Base32Rfc4648())
                .secureRandom(new SecureRandom())
                .securityProvider(Security.getProvider("BC"))
                .build());

String maskedId = idMask.mask(id128bit);
//example: RAKESQ32V37ORV5JX7FAWCFVV2PITRN5KMOKYBJBLNS42WCEA3FI2FIBXLKJGMTSZY
byte[] originalId = idMask.unmask(maskedId);
```

## Additional Features

### Upgrade of used Secret Key

If you want to change the secret key, because e.g. it became compromised, but still want to be able to unmask ids created
with the previous key, you can use the built-in key migration scheme. Since every created id encodes the id of the used key, it
is possible to choose a different key decryption.

Here is a full example: First a new instance with `key1` will be created. If no `key-id` is passed, the library uses
`KeyManager.Factory.DEFAULT_KEY_ID`.

```java
long id = 123456789L;
byte[] key1 = Bytes.random(16).array();

// create new instance with your key
IdMask<Long> idMask1 = IdMasks.forLongIds(Config.builder(key1).build());
String maskKey1 = idMask1.mask(id);
// e.g.: kpKOdqrNdmyx34-VxjTg6B4
```

If you want to switch the active key, just generate a new one and also set the old one as legacy key. Mind to choose
a different `key-id`:

```java
// if key1 is compromised create a new key
byte[] key2 = Bytes.random(16).array();

// set the new key as active key and add the old key as legacy key - us the DEFAULT_KEY_ID, is it is used if no key id is set
IdMask<Long> idMask2 = IdMasks.forLongIds(
        Config.builder(KeyManager.Factory.withKeyAndLegacyKeys(
                new KeyManager.IdSecretKey(KeyManager.Factory.DEFAULT_KEY_ID+1, key2), //new key with a new key id
                new KeyManager.IdSecretKey(KeyManager.Factory.DEFAULT_KEY_ID, key1))) //old key with the DEFAULT_KEY_ID
                .build());
```

Masking the same id, with the new key will generate different output:

```java
// same id will create different output
String maskKey2 = idMask2.mask(id);
// e.g.: 3c1UMVvVK5SvNiOaT4btpiQ
```

Unmasking however will reveal the same underlying id, no matter if it was masked with `key1` or `key2`.

```java
// the new instance can unmask the old an new key
assert idMask2.unmask(maskKey1).equals(idMask2.unmask(maskKey2));
```

_Be aware that changing the secret key, will destroy equality of masked ids cached with clients or elsewhere._

## Download

The artifacts are deployed to [jcenter](https://bintray.com/bintray/jcenter) and [Maven Central](https://search.maven.org/).

### Maven

Add dependency to your `pom.xml`:

    <dependency>
        <groupId>at.favre.lib</groupId>
        <artifactId>id-mask</artifactId>
        <version>{latest-version}</version>
    </dependency>

### Gradle

Add to your `build.gradle` module dependencies:

    compile group: 'at.favre.lib', name: 'id-mask', version: '{latest-version}'

### Local Jar

[Grab jar from latest release.](https://github.com/patrickfav/id-mask/releases/latest)

## Description

### Why?

IDMask can be used in an environment, where you want to protect the knowledge of the value of your IDs. Usually a very
easy workaround would be to add another column in your database and randomly create UUIDs and use this instead of your
e.g. numeric ids. However sometimes this is not feasible (e.g. having millions of rows) or cannot change the DB schema.
Additionally IDMask can make IDs appear random, a feature which cannot be satisfied with the above approach.

#### When to use IDMask

* If IDs are used which are easily guessable (ie. simple sequence) and knowledge of this ID might reveal confident information
* If IDs expose row count in a database table, which in turn reveals business intelligence (e.g. how many orders per day, etc.)
* For creating ad-hoc shareable links which should appear random to the public
* For creating single-use tokens for various use cases

#### When not to use IDMask

* If it is feasible to create a new column with random UUIDs
* If maximum performance is required

### Performance

IDMask requires a non-trivial amount of work to encrypt ids. The 8-byte-schema only needs to encrypt a single AES block (which should be hardware accelerated with most CPUs). The 16-byte schema is more expensive, since it requires encryption of an AES block, one HKDF expand and a HMAC calculation. According to the JMH benchmark, you can expect multiple hundreds encryption/decryption per ms. Compared to the performance HashIds, which is faster by a factor of about 1000, IDMask seems extremely slow, but in the grant scheme of things it probably doesn't make a difference if masking of a single id costs 2µs or 0.002µs.

#### JMH Benchmark

Here is an benchmark done on a [i7-7700k](https://ark.intel.com/content/www/us/en/ark/products/97129/intel-core-i7-7700k-processor-8m-cache-up-to-4-50-ghz.html):

```
Benchmark                                               Mode  Cnt      Score     Error  Units
IdMaskAndHashIdsBenchmark.benchmarkHashIdEncode         avgt    3      2,313 ±   0,125  ns/op
IdMaskAndHashIdsBenchmark.benchmarkHashIdEncodeDecode   avgt    3      3,279 ±   0,182  ns/op
IdMaskAndHashIdsBenchmark.benchmarkIdMask16Byte         avgt    3   7500,178 ± 397,964  ns/op
IdMaskAndHashIdsBenchmark.benchmarkIdMask8Byte          avgt    3   1841,921 ± 199,976  ns/op
IdMaskAndHashIdsBenchmark.benchmarkMaskAndUnmask16Byte  avgt    3  14964,914 ± 702,110  ns/op
IdMaskAndHashIdsBenchmark.benchmarkMaskAndUnmask8Byte   avgt    3   4032,200 ±  23,610  ns/op
```

### Encryption Schema

Two slightly different encryption schemes are used to optimize for performance and output size for the specific case. 
For each of these schemes two variation exist for deterministic and randomized encryption. 

#### 8 Byte Encryption Schema

This schema uses the following cryptographic primitives:

* [AES-128](https://en.wikipedia.org/wiki/Advanced_Encryption_Standard) + [ECB](https://en.wikipedia.org/wiki/Block_cipher_mode_of_operation#Electronic_Codebook_(ECB)) + [No Padding](https://en.wikipedia.org/wiki/Padding_(cryptography))

Using the a full 16 byte AES block, we create a message containing of the 8 byte id (ie. the plaintext) and an 8 byte
reference value. Then we encrypt it with AES/ECB (since we encrypt only a single block, a block mode using an IV like CBC
wouldn't make a difference):

    message_d = ( refValue_1a | id )
    maskedId_d = ciphertext_d = AES_ECB( message_d )

When decrypting, we compare the reference value, and if it has changed we discard the id, since either the key is incorrect,
or this was a forgery attempt:

    AES_ECB( maskedId_d ) = refValue_1b | id 
    refValue_1a == refValue_1b

##### Deterministic

In the deterministic mode the reference value is just a 8 byte long array of zeros.

##### Randomized

In the randomized mode the reference value is a random 8 byte long array. Because the decryption requires knowledge
of this value it will be prepended to the cipher text:

    ciphertext_r = AES_ECB( refValue_rnd | id )
    maskedId_r = refValue_rnd | ciphertext_r

##### Version Byte

Both modes have a version byte prepended which will be xor-ed with the first byte of the cipher text for simple obfuscation:

    obfuscated_version_byte = version_byte ^ ciphertext[0]
    
Finally the message looks like this:

    maskeId_msg_d = obfuscated_version_byte | maskedId_d
    
and     

    maskeId_msg_r = obfuscated_version_byte | maskedId_r

for randomized encryption.

##### Summary

This schema has the advantage of having forgery protection without the need for a dedicated MAC generation and also keeps
the output reasonable small with 16 + 1 byte.

#### 16 Byte Encryption Schema

This schema uses the following cryptographic primitives:

* [AES-128](https://en.wikipedia.org/wiki/Advanced_Encryption_Standard) + [CBC](https://en.wikipedia.org/wiki/Block_cipher_mode_of_operation#Cipher_Block_Chaining_(CBC)) + [No Padding](https://en.wikipedia.org/wiki/Padding_(cryptography))
* [HMAC-SHA256](https://en.wikipedia.org/wiki/HMAC)
* [HKDF-HMAC-SHA512](https://en.wikipedia.org/wiki/HKDF)


     ciphertext_d = AES_CBC( iv , id )
     mac_d = HMAC(ciphertext_d)
     maskeId_msg_d= ciphertext_d | mac_d[0-8]



### IDMask vs HashIds

One of the reasons this library was created, was that the author was not happy how HashIds solved the issue of
obfuscating/encryption of IDs. Here are the main criticism:

#### Weak, home-brew cryptography

HashIds encryption schema is basically: Have an alphabet. Shuffle it with Yates Algorithm and a salt provided by the user. Use this simple encoding schema to encode 53 bit integers. There is a well known [cryptanalysis](https://carnage.github.io/2015/08/cryptanalysis-of-hashids) and to be fair, the author of HashIds does not directly claim any security - BUT a library called HashId which is used to obfuscate IDs for security purpose which does not claim any actual security does not make sense to me.

#### Arbitrary limitation inherited rom the original javascript implementation

The integer values in HashId must be positive and have an upper bound of 53-bit (instead of 64-bit). If IDs are created in a sequence in the DB, this limitation is probably irrelevant, but if you work with random 64-bit values, this might break your code. IDMask does not apply any restriction on Java's `long`; the value may be positive as well as negative in the full range of 2^64. Only `BigInteger` is restricted to 15 byte (or 2^120).


In summary, here is simple comparison table of the main points:

|                    | IDMask                                    | HashId                       |
|--------------------|-------------------------------------------|------------------------------|
| Supported Types    | long, UUID, BigInteger, byte[], LongTuple | long, long[]                 |
| Type Limitations   | long: none, BigInteger: max 15 byte       | only positive and max 2^53   |
| Randomized IDs     | optional                                  | no                           |
| Output Length      | fixed length                              | variable length              |
| Encryption         | AES + HMAC                                | encoding + shuffled alphabet |
| Performance        | 2-7 µs/op                                 | 0.003 µs/op                  |
| Collision possible | no                                        | no                           |
| Caching            | Built-In                                  | no                           |
| Encodings          | Hex, Base32, Base64, Custom...            | customizable alphabet        |
| Forgery Protection | HMAC (8-16 bytes)                         | no                           |

## Security Relevant Information

### OWASP Dependency Check

This project uses the [OWASP Dependency-Check](https://www.owasp.org/index.php/OWASP_Dependency_Check) which is a utility that identifies project dependencies and checks if there are any known, publicly disclosed, vulnerabilities against a [NIST database](https://nvd.nist.gov/vuln/data-feeds).
The build will fail if any issue is found.

### Digital Signatures

#### Signed Jar

The provided JARs in the Github release page are signed with my private key:

    CN=Patrick Favre-Bulle, OU=Private, O=PF Github Open Source, L=Vienna, ST=Vienna, C=AT
    Validity: Thu Sep 07 16:40:57 SGT 2017 to: Fri Feb 10 16:40:57 SGT 2034
    SHA1: 06:DE:F2:C5:F7:BC:0C:11:ED:35:E2:0F:B1:9F:78:99:0F:BE:43:C4
    SHA256: 2B:65:33:B0:1C:0D:2A:69:4E:2D:53:8F:29:D5:6C:D6:87:AF:06:42:1F:1A:EE:B3:3C:E0:6D:0B:65:A1:AA:88

Use the jarsigner tool (found in your `$JAVA_HOME/bin` folder) folder to verify.

#### Signed Commits

All tags and commits by me are signed with git with my private key:

    GPG key ID: 4FDF85343912A3AB
    Fingerprint: 2FB392FB05158589B767960C4FDF85343912A3AB

## Build

### Jar Sign

If you want to jar sign you need to provide a file `keystore.jks` in the
root folder with the correct credentials set in environment variables (
`OPENSOURCE_PROJECTS_KS_PW` and `OPENSOURCE_PROJECTS_KEY_PW`); alias is
set as `pfopensource`.

If you want to skip jar signing just change the skip configuration in the
`pom.xml` jar sign plugin to true:

    <skip>true</skip>

### Build with Maven

Use the Maven wrapper to create a jar including all dependencies

    mvnw clean install

## Tech Stack

* Java 7 (+ [errorprone](https://github.com/google/error-prone) static analyzer)
* Maven

## Further Reading

### Discussions

* [Exposing database IDs - security risk?](https://stackoverflow.com/questions/396164/exposing-database-ids-security-risk)
* [Prevent Business Intelligence Leaks by Using UUIDs Instead of Database IDs on URLs and in APIs](https://medium.com/lightrail/prevent-business-intelligence-leaks-by-using-uuids-instead-of-database-ids-on-urls-and-in-apis-17f15669fd2e)
* [Why not expose a primary key](https://softwareengineering.stackexchange.com/questions/218306/why-not-expose-a-primary-key)
* [Sharding & IDs at Instagram](https://instagram-engineering.com/sharding-ids-at-instagram-1cf5a71e5a5c)
* [HashId Cryptanalysis](https://carnage.github.io/2015/08/cryptanalysis-of-hashids)

### Similar Libraries

* [HashIds](https://github.com/10cella/hashids-java)
* [NanoId](https://github.com/ai/nanoid)

# License

Copyright 2019 Patrick Favre-Bulle

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
