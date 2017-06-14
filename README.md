# AdGear Deploy SBT-Plugin

AdGear Deploy is an SBT plugin to group all the shared custom settings for SBT
builds within AdGear projects.

## Running tests

The command to run tests is `sbt scripted`

## Installing

To install this plugin in a project, simply add the following line to
`${PROJECT_ROOT_DIR}/project/plugins.sbt`:

`addSbtPlugin("com.adgear.data" %% "sbt-adgear-deploy" % "${VERSION_OF_THE_PLUGIN_HERE}")`


This Plugin is composed of the following subplugins:

- AdGearAssemblerPlugin: Enables `assembly` on a plugin, customizes the
  `assembly` plugin according to AdGear's preferences (more info can be
  extracted from the source code, it should be self-documentary). Makes sure that
  the fat jars are published, and that `package` triggers `assembly` command.
- AdGearShellCheckPlugin: Introduces an SBT input task, which takes space separated
  command line arguments, and runs `shellcheck` on them.
- AdGearDistributionPlugin: Marks the current project as a `distribution` project.
  Among others, publishes the tarball, and runs shellcheck on the shell scripts
  alongside the test command.

## Customizing the plugins

### AdGearAssemblerPlugin introduces the following settings:

- `targetDistributionDir` expects a `java.io.File`, and defaults to:
  `distribution/target`. This is where the fat jars exist.
- `prepareForTarball` a boolean flag to specify if the jar should end directly in the
   target dir or be prepared for inclusion in the tarball, i.e. should end in the lib
   directory as accustomed by AdGear, defaults to `false`.
- `jarName` expects `java.lang.String`, and sets the prefix of the
   produced jar name. Defaults to `ARTIFACT_ID-VERSION`.
- `distributedProjectName` expects a `java.lang.String`, and defaults to
  the name of the project that enables this plugin. This is used to customize
  the name of directory that the project tarball extracts to.
- `assemblyClassifier` extects a `java.lang.String`, and defaults to
  `jar-with-dependencies`. This is used to customize the name of the fat jars.

### AdGearDistributionPlugin introduces the following settings:

- `libDestDirName`: expects a `java.lang.String`, and defaults to `lib`.
  customizes the name of the `lib` folder in the tarball.
- `binDestDirName`: expects a `java.lang.String`, and defaults to `bin`.
  customizes the name of the `bin` folder in the tarball.
- `confDestDirName`: expects a `java.lang.String`, and defaults to `conf`.
  customizes the name of the `conf` folder in the tarball.
- `binSrcDir`: expects a `java.io.File`, and defaults to `src/main/scripts`,
  customizes the path of the `bin` directory that should end up in the tarball.
- `confSrcDir`: expects a `java.io.File`, and defaults to `src/main/resources/conf`,
  customizes the path of the `conf` directory that should end up in the tarball.
- `targetDir`: see the same option for AdGearAssemblerPlugin
- `projectName`: Name (or group id) of the distributed project, this determines
  the directory name that is archived. accepts a `java.lang.String` and
  defaults to the name of the project that enables it.
- `enableShellCheck`: A boolean flag, enables and disables running shellcheck on all
  the scripts that can be found in `binSrcDir` upon running test command in this project.
  defaults to `true`.

## Using the plugins:

None of the plugins are activated by default, to activate them they need to be enabled
explicitly. Let's say we have a project consisting of four subprojects:
- One is for distribution purposes (i.e everything related to
  generating the final tarball should go here). Called, `distribution`
- Another one, provides some utilities, but we don't want to produce a fat jar for it, called `core`.
- And two other projects, we want to have their fatjars end up in the final tarball:

Here is a simple `sbt` script to do it:

```
lazy val core = project(...).settings(...)

lazy val client = project(...).settings(...)
  .enablePlugins(AdGearAssemblerPlugin)
  .settings(plugin customization goes here)

lazy val server = project(...).settings(...)
  .enablePlugins(AdGearAssemblerPlugin)
  .settings(plugin customization goes here)

lazy val distribution = project(...).settings(...)
  .enablePlugins(AdGearDistributionPlugin)
  .settings(plugin customization goes here)

```

**To make sure that the final tarball contains all the fatjars** we need to package distribution
after all the projects that produce fatjars, this is done as follows:

```
lazy val distribution = project(...).settings(...)
  .enablePlugins(AdGearDistributionPlugin)
  .settings(Seq(plugin customization goes here,
    (packageBin in Compile) := ((packageBin in Compile) dependsOn (
    packageBin in Compile in server,
    packageBin in Compile in client)).value
))
```


# Producing Compact Jars

To produce compact Jar's you can use the shading rules of `sbt-assembly`
plugin. This should be done with caution, as dependencies that are only needed
through reflection might be dropped from the final fat jar.
