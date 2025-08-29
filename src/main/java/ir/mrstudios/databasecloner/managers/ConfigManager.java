package ir.mrstudios.databasecloner.managers;

import ir.mrstudios.databasecloner.models.YamlConfig;
import ir.mrstudios.databasecloner.utils.ConfigsUtil;
import ir.mrstudios.databasecloner.utils.DataUtil;
import lombok.Getter;
import lombok.Setter;

import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {

    @Getter
    @Setter
    private YamlConfig settingsYaml;

    public ConfigManager() {
        if (!Files.exists(Path.of("config.yml"))) {
            ConfigsUtil.loadConfigs();
            System.out.println("✅ Configs successfully created, please modify it and run again");
            System.exit(0);
            return;
        }
        this.settingsYaml = new YamlConfig(DataUtil.settingsFile);
    }

}
