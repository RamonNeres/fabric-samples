package org.example;

import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;

import org.apache.commons.codec.binary.Base64;
import sg.edu.ntu.sce.sands.crypto.dcpabe.*;
import org.bouncycastle.crypto.DataLengthException;
import sg.edu.ntu.sce.sands.crypto.dcpabe.key.PublicKey;
import sg.edu.ntu.sce.sands.crypto.utility.Utility;
import org.bouncycastle.crypto.InvalidCipherTextException;
import sg.edu.ntu.sce.sands.crypto.dcpabe.ac.AccessStructure;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;

public class ABEUtil {

    public static byte[] encrypt(byte[] clearFile, String policy, GlobalParameters gp, Authority... authorities)
            throws IOException, InvalidCipherTextException {
        Message m = DCPABE.generateRandomMessage(gp);

        PublicKeys pks = new PublicKeys();
        HashMap<String, PublicKey> pksMap = new HashMap<String, PublicKey>();
        //TODO: Checar possível bug na dcpabe que pode causar erros ao utilizar duas autoridades com mesmo atributo
        for (Authority auth : authorities)
            auth.getPublicKeys().forEach((att, pk) -> {
                byte[] eg1g1ai = Base64.decodeBase64(pk.getEg1g1ai());
                byte[] g1yi = Base64.decodeBase64(pk.getG1yi());

                pksMap.put(att, new PublicKey(eg1g1ai, g1yi));
            });
        pks.subscribeAuthority(pksMap);
        AccessStructure as = AccessStructure.buildFromPolicy(policy);
        Instant start = Instant.now();
        Ciphertext ct = DCPABE.encrypt(m, as, gp, pks);
        Instant end = Instant.now();
        long total = Duration.between(start, end).toMillis();
        //System.out.println("File ABE encrypt only\t" + total);

        try (
                ByteArrayOutputStream fos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(fos);

                ByteArrayInputStream fis = new ByteArrayInputStream(clearFile);
                BufferedInputStream bis = new BufferedInputStream(fis);
        ) {
            oos.writeObject(ct);
            Instant startAES = Instant.now();
            PaddedBufferedBlockCipher aes = Utility.initializeAES(m.getM(), true);

            oos.write(encryptOrDecryptPayload(aes, bis));
            Instant endAES = Instant.now();
            long totalAES = Duration.between(startAES, endAES).toMillis();
            //System.out.println("File AES encrypt only\t" + totalAES);
            oos.flush();
            return fos.toByteArray();
        }
    }

    public static byte[] encryptTest(byte[] clearFile, String policy, GlobalParameters gp, Authority auth, int repetitions)
            throws IOException, InvalidCipherTextException {
        Message m = DCPABE.generateRandomMessage(gp);

        PublicKeys pks = new PublicKeys();
        HashMap<String, PublicKey> pksMap = new HashMap<String, PublicKey>();
        //TODO: Checar possível bug na dcpabe que pode causar erros ao utilizar duas autoridades com mesmo atributo
        auth.getPublicKeys().forEach((att, pk) -> {
            byte[] eg1g1ai = Base64.decodeBase64(pk.getEg1g1ai());
            byte[] g1yi = Base64.decodeBase64(pk.getG1yi());

            pksMap.put(att, new PublicKey(eg1g1ai, g1yi));
        });
        pks.subscribeAuthority(pksMap);
        AccessStructure as = AccessStructure.buildFromPolicy(policy);

        Ciphertext ct = null;
        for (int i = 0; i < 1; i++) {
            Instant start = Instant.now();
            ct = DCPABE.encrypt(m, as, gp, pks);
            Instant end = Instant.now();
            long total = Duration.between(start, end).toMillis();
            //System.out.println("Key ABE encrypt only\t" + total);
        }

        try (
                ByteArrayOutputStream fos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(fos);

                ByteArrayInputStream fis = new ByteArrayInputStream(clearFile);
                BufferedInputStream bis = new BufferedInputStream(fis);
        ) {
            oos.writeObject(ct);

            Instant startAES = Instant.now();
            PaddedBufferedBlockCipher aes = Utility.initializeAES(m.getM(), true);
            for (int i = 0; i < repetitions; i++) {
                oos.write(encryptOrDecryptPayload(aes, bis));
                Instant endAES = Instant.now();
                long totalAES = Duration.between(startAES, endAES).toMillis();
                //System.out.println("File AES encrypt only\t" + totalAES);
            }

            oos.flush();
            return fos.toByteArray();
        }
    }

    public static byte[] decrypt(byte[] cipheredBytes, PersonalKeys perKeys, GlobalParameters gp, String... policies)
            throws InvalidCipherTextException, IOException, ClassNotFoundException {
        try (
                ByteArrayInputStream input = new ByteArrayInputStream(cipheredBytes);
                ObjectInputStream oIn = new ObjectInputStream(input)
        ) {
            Instant startExtractKey = Instant.now();
            Ciphertext ct = Utility.readCiphertext(oIn);
            Instant endExtractKey = Instant.now();
            long timeExtract = Duration.between(startExtractKey, endExtractKey).toMillis();
            //System.out.println("extração da chave criptografada\t" + timeExtract);

            Instant startDecriptKey = Instant.now();
            Message m = DCPABE.decrypt(ct, perKeys, gp);
            Instant endDecriptKey = Instant.now();
            long timeDecript = Duration.between(startDecriptKey, endDecriptKey).toMillis();
            //System.out.println("decriptação da chave com ABE\t" + timeDecript);

            Instant startDecriptFile = Instant.now();
            PaddedBufferedBlockCipher aes = Utility.initializeAES(m.getM(), false);
            byte[] clearFile = encryptOrDecryptPayload(aes, oIn);
            Instant endDecriptFile = Instant.now();
            long timeDecriptFile = Duration.between(startDecriptFile, endDecriptFile).toMillis();
            //System.out.println("decriptação do arquivo com AES\t" + timeDecriptFile);

            return clearFile;
        }
    }

    public static byte[] decryptTest(byte[] cipheredBytes, PersonalKeys perKeys, GlobalParameters gp, int repetitions, String... policies)
            throws InvalidCipherTextException, IOException, ClassNotFoundException {
        Message m = null;
        try (
                ByteArrayInputStream input = new ByteArrayInputStream(cipheredBytes);
                ObjectInputStream oIn = new ObjectInputStream(input)
        ) {
            Instant startExtractKey = Instant.now();
            Ciphertext ct = Utility.readCiphertext(oIn);
            Instant endExtractKey = Instant.now();
            long timeExtract = Duration.between(startExtractKey, endExtractKey).toMillis();
            //System.out.println("extração da chave criptografada\t" + timeExtract);


            for (int i = 0; i < repetitions; i++) {
                Instant startDecriptKey = Instant.now();
                m = DCPABE.decrypt(ct, perKeys, gp);
                Instant endDecriptKey = Instant.now();
                long timeDecript = Duration.between(startDecriptKey, endDecriptKey).toMillis();
                //System.out.println("decriptação da chave com ABE\t" + timeDecript);
            }
        }

        byte[] clearFile = null;
        for (int i = 0; i < 1; i++) {
            try (ByteArrayInputStream input = new ByteArrayInputStream(cipheredBytes);
                 ObjectInputStream oIn = new ObjectInputStream(input)) {
                Ciphertext ct = Utility.readCiphertext(oIn);
                Instant startDecriptFile = Instant.now();
                PaddedBufferedBlockCipher aes = Utility.initializeAES(m.getM(), false);
                clearFile = encryptOrDecryptPayload(aes, oIn);
                Instant endDecriptFile = Instant.now();
                long timeDecriptFile = Duration.between(startDecriptFile, endDecriptFile).toMillis();
                //System.out.println("decriptação do arquivo com AES\t" + timeDecriptFile);
            }
        }
        return clearFile;
    }

    private static byte[] encryptOrDecryptPayload(PaddedBufferedBlockCipher cipher, InputStream is)
            throws DataLengthException, IllegalStateException, InvalidCipherTextException, IOException {
        return encryptOrDecryptPayload(cipher, is, new byte[0]);
    }

    private static byte[] encryptOrDecryptPayload(PaddedBufferedBlockCipher cipher, InputStream is, byte[] prefix)
            throws DataLengthException, IllegalStateException, InvalidCipherTextException, IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] inBuff = new byte[cipher.getBlockSize()];
        byte[] outBuff = new byte[cipher.getOutputSize(inBuff.length)];
        os.write(prefix);
        int nbytes;
        while (-1 != (nbytes = is.read(inBuff, 0, inBuff.length))) {
            int length1 = cipher.processBytes(inBuff, 0, nbytes, outBuff, 0);
            os.write(outBuff, 0, length1);
        }
        nbytes = cipher.doFinal(outBuff, 0);
        os.write(outBuff, 0, nbytes);

        return os.toByteArray();
    }

}