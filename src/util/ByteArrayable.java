package util;

import java.io.*;

/**
 * @author Henrik Akesson
 * @author Fabien Salathe
 * Trait
 * to serialize an object to an array of bytes
 * or to unserialize an array of bytes to an object
 */
public interface ByteArrayable {
    default byte[] toByteArray() throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.flush();
                oos.writeObject(this);
                oos.flush();
            }

            return baos.toByteArray();
        }
    }

    static Object fromByteArray(final byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
            try (ObjectInputStream ois = new ObjectInputStream(bais)) {
                Object o1 = ois.readObject();
                ois.close();
                return o1;
            }
        }
    }
}
