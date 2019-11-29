# Aurora Token Dispenser

### Building
1. `git clone https://github.com/yeriomin/token-dispenser`

2. `cd token-dispenser`

3. `src/main/resources/config.properties` Edit this file to match your likings

4. `./gradlew build`

Post successful compilation, you can find the jar from `/build/libs/`

### Configuration

[config.properties](/src/main/resources/config.properties) holds  dispenser configuration.

Configurable parameters:
* host (default 0.0.0.0)
* port (default 8080)
* credentials (default auth.txt, contains EMAIL,AAS_TOKEN pairs seperated by space, one per line)
* keystore - name of you keystore file (.jks)
* keystore_password - your keystore password

keystore and keystore_password are required only if you wich to enforce https, all dependent files are placed in `/build/libs/`

Once configuration is done, you can start the dispenser by following command:

`java -jar /build/libs/token-dispenser-1.1.jar`

### Usage
Once server is configured, you can get the following.
1. Status : http://server-address:port/status
2.  Email : http://server-address:port/email
3. Tokens  : http://server-address:port/token/email/youremail@gmail.com

### Credits

* [play-store-api](https://github.com/yeriomin/play-store-api)
* [spark](http://sparkjava.com/)
