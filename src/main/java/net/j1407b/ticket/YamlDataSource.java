package net.j1407b.ticket;


import dev.dejvokep.boostedyaml.YamlDocument;

import java.io.File;
import java.io.IOException;

public class YamlDataSource {
    private static final YamlDocument config;
    static {
        try {
            config = YamlDocument.create(new File("config.yml"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static YamlDocument getConfig() {
        return config;
    }
}
