package ir.mrstudios.databasecloner.enums;

import ir.mrstudios.databasecloner.Main;
import ir.mrstudios.databasecloner.utils.NumberUtil;
import lombok.SneakyThrows;

public enum Config {

    //MONGO
    MONGO_ENABLE("mongo.enable"),
    MONGO_URI("mongo.uri"),
    MONGO_THREADS("mongo.threads"),
    MONGO_PATH("mongo.path"),
    MONGO_INTERVAL("mongo.interval"),

    //MYSQL
    MYSQL_ENABLE("mysql.enable"),
    MYSQL_HOST("mysql.host"),
    MYSQL_PORT("mysql.port"),
    MYSQL_USERNAME("mysql.username"),
    MYSQL_PASSWORD("mysql.password"),
    MYSQL_THREADS("mysql.threads"),
    MYSQL_PATH("mysql.path"),
    MYSQL_INTERVAL("mysql.interval");

    private Object value;
    private final String path;

    Config(String path) {
        this.path = path;
        this.load();
    }

    @SuppressWarnings("unchecked")
    public <T> T getAs(Class<T> clazz) {
        if (this.value instanceof Number number) {
            return NumberUtil.cast(number, clazz);
        }
        return (T) this.value;
    }

    @SneakyThrows
    private void load() {
        this.value = Main.getInstance().getConfigManager().getSettingsYaml().get(this.path);
    }
}
