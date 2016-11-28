package util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigReader {
    public static List<MachineAddress> read(File file) throws IOException {
        if (file.exists()) {
            List<MachineAddress> machines = Files
                    .lines(file.toPath())
                    .map(s -> {
                        String[] token = s.split(":", 2);
                        if (token.length == 2) {
                            return new MachineAddress(token[0], Integer.parseInt(token[1]));
                        }

                        return null;
                    })
                    .filter(m -> m != null)
                    .collect(Collectors.toList());

            return machines;
        }

        return null;
    }
}
