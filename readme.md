# Forest

### Installation

Download and extract the [latest release](https://github.com/delfikpro/forest/releases).  
Run `source executeme.sh` to add forest commands into your `PATH` variable.

Run `forest HELLO-1` to verify your installation!

```shell
mkdir ~/forest
cd ~/forest
wget https://github.com/delfikpro/forest/releases/download/4.0.0/forest-4.0.0.zip
unzip forest-4.0.0.zip
source executeme.sh
```
### Usage

Command `forest [realm id]` creates a realm (or uses existing) and launches it.  
Example realm ids: `MC-1`, `MC-2`, `MC-TEST-1`, `OWO-777`

You can specify preset manually using `--preset`, or by calling `map ... to ...` method.

You can use `berry` command to repeatedly restart `forest`  

### Configuration

The `presets` directory contains `.groovy` configs.

```groovy
preset somePreset: {

    // Copies someResource.jar from resources directory into local realm folder
    resourceCopy 'someResource.jar' 
    
    // Copies someArchive.tar while changing the name
    resourceCopy 'someArchive.tar', 'test/archive.tar'
    
    // Executes shell command (inside is realm dir)
    execute 'tar xf test/archive.tar'
    
    // Executes java command
    java {
        
        // Java executable path (default is 'java')
        javaPath '/home/user/java-6/bin/java'
        
        jvmArgs '-Djava.library.path=natives', '-XX:MaxPermSize=64M'
        
        // Shortcuts for -Xmx and -Xms
        xmx '128M'
        xms '8M'
        
        // Program arguments
        arguments '--someArgument'
        
        // Add resource from realm dir to classpath
        classpath 'someResource.jar'
        // Add resource from global resources dir to classpath without copying
        classpath resource('otherResource.jar')
       
        // Java program entrypoint
        mainClass 'com.example.seriouscompany.seriousapplication.App'
        
    }
    
    // Deletes files/directories
    delete 'tmp'
    
}

// Add automatic mapping for realm 'TEST-1' to preset 'somePreset'
// So that you can write 'forest TEST-1' without specifying the --preset flag
map 'TEST-1' to 'somePreset'

// Mappings do support regular expressions.
map 'TEST-.+' to 'somePreset'
```