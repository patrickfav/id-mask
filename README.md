# ID Mask

Id mask is a library for masking public (database) ids to hide the actual value of the id. This should make it harder for an attacker to guess or understand provided ids. Additionally it is possible to generate non-deterministic ids for e.g. shareable links or single use tokens. This library has a similar goal as [HashIds](https://hashids.org/) but depends in contrast on cryptographically strong algorithms.

[![Download](https://api.bintray.com/packages/patrickfav/maven/id-mask/images/download.svg)](https://bintray.com/patrickfav/maven/id-mask/_latestVersion)
[![Build Status](https://travis-ci.org/patrickfav/id-mask.svg?branch=master)](https://travis-ci.org/patrickfav/id-mask)
[![Javadocs](https://www.javadoc.io/badge/at.favre.lib/id-mask.svg)](https://www.javadoc.io/doc/at.favre.lib/id-mask)
[![Coverage Status](https://coveralls.io/repos/github/patrickfav/id-mask/badge.svg?branch=master)](https://coveralls.io/github/patrickfav/id-mask?branch=master) [![Maintainability](https://api.codeclimate.com/v1/badges/fc50d911e4146a570d4e/maintainability)](https://codeclimate.com/github/patrickfav/id-mask/maintainability)

## Quickstart

Add dependency to your `pom.xml` ([check latest release](https://github.com/patrickfav/id-mask/releases)):

    <dependency>
        <groupId>at.favre.lib</groupId>
        <artifactId>id-mask</artifactId>
        <version>{latest-version}</version>
    </dependency>

A very simple example using 64 bit integers (`long`):

```java
byte[] key = Bytes.random(16).array();
long id = ...

IdMask<Long> idMask = IdMasks.forLongIds(Config.builder().key(key).build());

String maskedId = idMask.mask(id);
//example: rK0wpnG1lwvG0xiZn5swxOYmAvxhA4A7yg
long originalId = idMask.unmask(maskedId);
```

and using UUIDs

```java
byte[] key = Bytes.random(16).array();
UUID id = UUID.fromString("eb1c6999-5fc1-4d5f-b98a-792949c38c45");

IdMask<UUID> idMask = IdMasks.forUuids(Config.builder().key(key).build());

String maskedId = idMask.mask(id);
//example: rK0wpnG1lwvG0xiZn5swxOYmAvxhA4A7yg
UUID originalId = idMask.unmask(maskedId);
```

## How-To

### Step 1: Create a Secret Key

The base of the security of IdMask relies on the strength of the used key. A key is basically just a 
random byte array. A key should be at least 16 bytes long (longer usually doesn't translate to better security).
IdMask requires the key to be 8 and 64 bytes long. There are multiple ways to manage secret keys, if your project already has a managed [`KeyStore`](https://docs.oracle.com/javase/7/docs/api/java/security/KeyStore.html), use it. In it's simplest form, you can just hardcode the key. This is of course only makes sense, where the client doesn't have access to the code or binary (i.e. in a backend scenario).

#### Option A: Use Random Number Generator CLI

One of the easiest ways to create a high quality key is to use this random java cli: [Dice](https://github.com/patrickfav/dice/releases). Just download the `.jar` (or `.exe`)

    java -jar dice.jar 16 -e "java"

This will generate multiple 16 byte long syntactically correct java byte arrays:

    new byte[]{(byte) 0xE4, (byte) 0x8A, ...};

You could just hard code this value:

    private static final byte[] ID_MASK_KEY = new byte[]{(byte) 0xE4, (byte) 0x8A, ...};
    
#### Option B: Generate Random Key within Java Code

Either in the [debugger](https://www.jetbrains.com/help/idea/debugging-your-first-java-application.html), simple application or any other [REPL](https://en.wikipedia.org/wiki/Read%E2%80%93eval%E2%80%93print_loop) execute the following code (IdMask must be in classpath):

    Bytes.random(16).encodeHex();

Which will create a random byte array using SecureRandom and encodes it as hex string. In your code just 
use this code and proved the hex string created before:

    private static final byte[] ID_MASK_KEY = Bytes.parseHex("e48a....").array();

Either way, don't worry too much as the library supports changing the secret key while still be able to 
unmask older ids.

### Step 2: Initialize IdMask

Usually the default settings are fine for most use cases, however often the following may make sense to change some settings based on current requirements

#### Q1: Should Ids be deterministic or random?

By default off, the masking algorithm supports randomization of generated ids. This is achieved by creating a random number, use it as part to encrypt the id and append it the output. Therefore randomized Ids are longer than their deterministic counter part. Randomization increases the obfuscation effectiveness but makes it impossible for a client to check equality. This usually makes sense with shareable links, random access tokens, or other one-time identifiers. Randomized ids withing models are probably a bad idea. 

Enable with:

    Config.builder()
        .randomizedIds(true)
        ...

#### Q2: What encoding should I choose?

The library internally converts everything to bytes, encrypts it and then requires an encoding schema to make the output printable. Per default the url-safe version of Base64 ([RFC4648](https://tools.ietf.org/html/rfc4648)) is used. This is a well supported, fast and reasonable space efficient (needs ~25% more storage than the raw bytes) encoding.

Depending on your use case, you may want Ids that:

* are easy to type
* do not contain words, or at the very least curse words
* need to be as short as possible

Currently the following encodings are supported:


| Encoding               | may contain words | easy to type                        | Length for 64 bit id (deterministic/randomized) | Length for 128 bit id (deterministic/randomized) | Example                              |
|------------------------|-------------------|-------------------------------------|-------------------------------------------------|--------------------------------------------------|--------------------------------------|
| Hex                    | no                | yes                                 | 34/50                                           | 50/82                                            | `e5e53e09bbd37f8d8b9afdfbed776de6fe` |
| Base32                 | yes               | yes | 28/40                                           | 40/66                                            | `XS6GLNDNQ2NSBWJRMWM3U72FTLLA`       |
| Base32 (Safe Alphabet) | no curse words    | contains upper and lowercase        | 28/40                                           | 40/66                                            | `pVY2YYbV8GyzaEZ3aB5b87EeP4Da`       |
| Base64                 | yes               | no                                  | 23/34                                           | 34/55                                            | `SkqktDj1MVEkiPMrwg1blfA`            |


 * **Caching**: By default a simple in-memory cache is enabled. You may want to provide your own cache if you still use one.
 * Advanced Security Features

```java
IdMask<byte[]> idMask = IdMaskFactory.createFor128bitNumbers(
        Config.builder()
                .keyManager(KeyManager.Factory.with(key))
                .randomizedIds(true) //non-deterministic output
                .enableCache(true)
                .cacheImpl(new Cache.SimpleLruMemCache(64))
                .encoding(new ByteToTextEncoding.Base32())
                .secureRandom(new SecureRandom())
                .securityProvider(Security.getProvider("BC"))
                .build());

String maskedId = idMask.mask(id128bit);
//example: RAKESQ32V37ORV5JX7FAWCFVV2PITRN5KMOKYBJBLNS42WCEA3FI2FIBXLKJGMTSZY
byte[] originalId = idMask.unmask(maskedId);
```

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

tba.

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

## Similar Libraries

* [HashIds](https://github.com/10cella/hashids-java)

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
