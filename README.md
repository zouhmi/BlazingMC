# BlazingMC

Custom Minecraft: Java Edition server implementation targeting protocol 1.20.4.

## Build

```bash
./gradlew clean build
```

## Test with 1 GB RAM

Install the supplied plugin JARs into the local test directory:

```bash
./setup-test-plugins.sh
```

Start BlazingMC with a 1 GB heap:

```bash
./run-test-server.sh
```

Use a different plugin directory with:

```bash
BLAZINGMC_PLUGIN_DIR=/path/to/plugins ./run-test-server.sh
```

The installer excludes the duplicate `Chunky-Bukkit-1.4.40(1).jar` file. The loader scans plugin metadata, checks required dependencies, isolates class loaders, registers commands and events, schedules tasks, and reports unsupported plugins without stopping the server.

The supplied JARs were built for Bukkit, Spigot, Paper, or newer API versions. BlazingMC provides a native compatibility layer, but plugins that require unimplemented Paper internals, newer Java bytecode, or unavailable dependencies remain disabled and are reported during startup.

## Run without plugins

```bash
./gradlew :server-core:build
java -Xms1G -Xmx1G -XX:+UseG1GC -jar server-core/build/libs/server-core-0.1.0-dev.jar
```

## License

Free to use. Not for sale.

Created by @Zouhmi - zouhmi.com
