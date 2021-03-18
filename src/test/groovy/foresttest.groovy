@groovy.transform.BaseScript forest.ForestScript script

preset cristalixAuth: {

    env TOWER_IP: 'tower.cristalix.ru'
    env TOWER_PORT: 12345
    env TOWER_LOGIN: 'forest'
    env TOWER_PASSWORD: 'veryStrongPassw0rd'
    env TOWER_PATH: '/'
    env REALM_TYPE: realmType
    env REALM_ID: realmId

}

preset minimalBukkit: {
    resourceCopy 'minimal/server.properties', 'server.properties'
    resourceCopy 'minimal/bukkit.yml', 'bukkit.yml'
    resourceCopy 'minimal/spigot.yml', 'spigot.yml'
    resourceCopy 'minimal/paper.yml', 'paper.yml'
}

preset cristalixCore: {
    resourceCopy 'bukkit-core-21.01.05.jar', 'plugins/cristalix-core.jar'
}

map brawlCommon: /BST-1/

preset brawlCommon: {


    resourceCopy 'brawl-stars/'

    load presets.minimalBukkit
    load presets.cristalixAuth
    load presets.cristalixCore


    env BRAWL_SERVICE: '127.0.0.1:25897'

    java {
        classpath resource('paper-libs') + '/*'
        classpath resource('dark-paper-21.01.06.2.jar')
        classpath resource('launcher.jar')

        mainClass 'org.bukkit.craftbukkit.Main'
        arguments '--port', assignedPort
    }

    delete 'tmp/', 'logs/'

}

preset brawlGame: {
    resourceCopy "brawlstars-lakai/brawlstars-bukkit-games.jar", "plugins/brawlstars-bukkit-town.jar"
    load presets.brawlCommon
}

preset brawlLobby: {
    resourceCopy "brawlstars-lakai/brawlstars-bukkit-town.jar", "plugins/brawlstars-bukkit-town.jar"
    load presets.brawlCommon
}

preset brawlService: {

    load presets.cristalixAuth
    env BRAWL_SERVICE_PORT: realmId < 0 ? 25896 : 25894


    java {
        classpath resourcePath('brawlstars-lakai/brawlstars-manager.jar')
        classpath resourcePath('microservice-1.0.4.jar')
        classpath resourcePath('spigot.jar')

        mainClass 'brawlstars.manager.ManagerService'
    }


}





