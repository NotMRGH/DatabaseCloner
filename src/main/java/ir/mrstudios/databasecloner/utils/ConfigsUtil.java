package ir.mrstudios.databasecloner.utils;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@UtilityClass
public class ConfigsUtil {

    @SneakyThrows
    public void loadConfigs() {

        final Path destinationPath = Path.of("config.yml");
        if (Files.exists(destinationPath)) return;

        final InputStream inputStream = ConfigsUtil.class.getResourceAsStream("/" + "config.yml");
        if (inputStream == null) return;
        Files.copy(inputStream, destinationPath, StandardCopyOption.REPLACE_EXISTING);
        inputStream.close();

    }
}
