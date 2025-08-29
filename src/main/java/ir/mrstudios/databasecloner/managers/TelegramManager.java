package ir.mrstudios.databasecloner.managers;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendDocument;
import com.pengrad.telegrambot.response.SendResponse;
import ir.mrstudios.databasecloner.enums.Config;
import okhttp3.OkHttpClient;

import java.io.File;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

public class TelegramManager {

    private final TelegramBot telegramBot;

    public TelegramManager() {
        if (Config.SOCKS5_ENABLE.getAs(Boolean.class) && Config.TELEGRAM_ENABLE.getAs(Boolean.class)) {
            final Proxy proxy = new Proxy(
                    Proxy.Type.SOCKS,
                    new InetSocketAddress(
                            Config.SOCKS5_HOST.getAs(String.class), Config.SOCKS5_PORT.getAs(Integer.class)
                    )
            );

            if (
                    !Config.SOCKS5_USERNAME.getAs(String.class).isEmpty() &&
                    !Config.SOCKS5_PASSWORD.getAs(String.class).isEmpty()
            ) {
                Authenticator.setDefault(new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        if (this.getRequestorType() == RequestorType.PROXY) {
                            return new PasswordAuthentication(
                                    Config.SOCKS5_USERNAME.getAs(String.class),
                                    Config.SOCKS5_PASSWORD.getAs(String.class).toCharArray()
                            );
                        }
                        return null;
                    }
                });
            }

            final OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .proxy(proxy)
                    .build();

            this.telegramBot = new TelegramBot.Builder(Config.TELEGRAM_TOKEN.getAs(String.class))
                    .okHttpClient(client)
                    .build();
        } else if (Config.TELEGRAM_ENABLE.getAs(Boolean.class)) {
            this.telegramBot = new TelegramBot(Config.TELEGRAM_TOKEN.getAs(String.class));
        } else {
            this.telegramBot = null;
        }
    }

    public void sendBackup(File file) {
        if (this.telegramBot == null) return;
        final SendResponse execute = this.telegramBot.execute(
                new SendDocument(Config.TELEGRAM_USER_ID.getAs(Long.class), file)
        );

        if (!execute.isOk()) {
            System.out.println("❌ Couldn't send backup because " + execute.description());
            return;
        }

        System.out.println("✅ Backup sent successfully.");
    }
}
