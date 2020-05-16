package org.hyperledger.fabric.samples.fabcar;

import java.io.File;
import java.io.IOException;

import java.nio.charset.StandardCharsets;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;


public class Util {
    public static String encodeFileToBase64Binary(String fileName) throws IOException {
        File file = new File(fileName);
        byte[] encoded = Base64.encodeBase64(FileUtils.readFileToByteArray(file));
        return new String(encoded, StandardCharsets.US_ASCII);
    }

    public static void base64toFile(String fileName, String base64File) throws IOException {
        File file = new File(fileName);
        file.mkdirs();
        FileUtils.writeStringToFile(file, base64File, StandardCharsets.US_ASCII);
    }
}
