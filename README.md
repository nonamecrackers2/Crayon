# Crayon
[![Discord](https://img.shields.io/discord/987817685293355028?style=flat-square&logo=discord&label=Discord&color=%235865F2)](https://discord.gg/cracker-s-modded-community-987817685293355028)

Rigid body simulation in Minecraft.

A fork of [Rayon](https://github.com/LazuriteMC/Rayon) to continue the project exclusively for Forge.

## Examples
See the [examples folder](https://github.com/nonamecrackers2/Crayon/tree/1.19.4/src/main/java/dev/lazurite/rayon/impl/example) for an example

## Developing with Crayon
Add the following lines to your `build.gradle`:

```gradle
repositories {
    maven {
        name "nonamecrackers2Maven"
        url "https://maven.nonamecrackers2.dev/snapshots"
    }
    mavenCentral()
}

dependencies {
    implementation fg.deobf("nonamecrackers2:crackerslib-forge:${crackerslib_version}")
    implementation fg.deobf("nonamecrackers2:crayon-forge:${crayon_version}")
    minecraftLibrary "com.github.stephengold:Libbulletjme:${libbulletjme_version}"
}
```

Define ``crackerslib_version``, ``crayon_version``, and ``libbulletjme_version`` in your ``gradle.properties`` file. **Please refer to [this page](https://github.com/nonamecrackers2/Crayon/wiki/Versions) on what versions to use for each dependency.**
