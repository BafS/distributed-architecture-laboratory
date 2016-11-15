package util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigReader {

//    public static String read(String filename) {
//        return read(new File(filename));
//    }

    public static List<String[]> read(File file) throws IOException {
        //List<Machine> machines = new ArrayList<>();

        if (file.exists()) {
            List<String[]> machines = Files.lines(file.toPath()).map(s -> {
                return s.split(":");
                //return new Machine(i[0], new Integer(i[1]).intValue());
            }).collect(Collectors.toList());

            return machines;
        }

        return null;
    }
}
