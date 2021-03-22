# Forest

### Установка

Скачайте и запустите `install-forest.sh` из последнего релиза.

### Использование

Команда `forest [Реалм]` исполняет пресет.  
Пресет можно указать аргументом `--preset`, либо используя метод `map` в конфигурации.

Команда `berry` запускает `forest` в бесконечном цикле.  

### Конфигурация

В папке `presets` содержатся конфиги на языке groovy.

Основные методы в конфигах - `preset` и `map`.

```groovy
preset somePreset: {

    // Копирует файл someResource.jar из папки ресурсов в папку сервера
    resourceCopy 'someResource.jar' 
    
    // Копировать файл someArchive.tar и меняет название
    resourceCopy 'someArchive.tar', 'test/archive.tar'
    
    // Выполнить команду
    execute 'tar xf test/archive.tar'
    
    // Выполнить команду java
    java {
        
        // Указать путь к джаве (стандартный - 'java')
        javaPath '/home/user/java-6/bin/java'
        
        // Добавить аргументы JVM
        jvmArgs '-Djava.library.path=natives', '-XX:MaxPermSize=64M'
        
        // Короткие методы для -Xmx и -Xms
        xmx '128M'
        xms '8M'
        
        // Добавить аргументы софта
        arguments '--someArgument'
        
        // Добавить classpath
        classpath 'someResource.jar'
        // Ресурсы для classpath необязательно копировать
        classpath resource('otherResource.jar')
        
        // Главный класс
        mainClass 'com.example.seriouscompany.seriousapplication.App'
        
    }
    
    // Удалить файлы
    delete 'tmp'
    
}

// Добавить автоматический маппинг TEST-1 к пресету somePreset
// Чтобы не нужно было писать forest --preset somePreset TEST-1
map 'TEST-1' to 'somePreset'

// map поддерживает regex
map 'TEST-.+' to 'somePreset'
```