package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import sg.edu.ntu.sce.sands.crypto.dcpabe.AuthorityKeys;
import sg.edu.ntu.sce.sands.crypto.dcpabe.DCPABE;
import sg.edu.ntu.sce.sands.crypto.dcpabe.GlobalParameters;
import sg.edu.ntu.sce.sands.crypto.dcpabe.key.PublicKey;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Util {

    public static <T extends Serializable> void writeObjToJSON (String fileName, T object){
        ObjectMapper mapper = new ObjectMapper();

        try {
            File file = new File(fileName);
            mapper.writeValue(file, object);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static <T extends Serializable> T readFileFromJSON(String fileName, Class<T> tClass){
        T obj = null;

        try {
            ObjectMapper mapper = new ObjectMapper();
            File file = new File(fileName);
            obj = mapper.readValue(file, tClass);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return obj;
    }

    public static byte[] objToByteArray(Object obj) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        oos.flush();
        return bos.toByteArray();
    }

    public static <T extends Serializable> T readObjFromJSON(byte[] data, Class<T> tClass){
        T obj = null;

        try {
            ObjectMapper mapper = new ObjectMapper();
            obj = mapper.readValue(data, tClass);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return obj;
    }

    public static <T> T convertFromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInput in = new ObjectInputStream(bis)) {
            return (T)(in.readObject());
        }
    }

    public static void saveSecretKeys(AuthorityKeys ak){
        String folder = ak.getAuthorityID();
        File fol = new File(folder);
        fol.mkdirs();
        ak.getSecretKeys().forEach((pol, sec) -> {
            Util.writeObjToJSON(folder+"/"+pol+".json", sec);
        });
    }

    public static String getBase64Eg1g1ai(byte[] eg1g1ai) {
        return new String(java.util.Base64.getEncoder().encode(eg1g1ai));
    }

    public static String getBase64G1yi(byte[] g1yi) {
        return new String(java.util.Base64.getEncoder().encode(g1yi));
    }

    public static String encodeFileToBase64Binary(String fileName) throws IOException {
        File file = new File(fileName);
        byte[] encoded = Base64.encodeBase64(FileUtils.readFileToByteArray(file));
        return new String(encoded, StandardCharsets.US_ASCII);
    }
}
