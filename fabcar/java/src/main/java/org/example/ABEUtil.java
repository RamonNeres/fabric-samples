package org.example;


import java.io.*;
import java.util.Scanner;



import com.fasterxml.jackson.databind.ObjectMapper;
import sg.edu.ntu.sce.sands.crypto.DCPABETool;
import sg.edu.ntu.sce.sands.crypto.dcpabe.*;
import org.bouncycastle.crypto.DataLengthException;
import sg.edu.ntu.sce.sands.crypto.utility.Utility;
import org.bouncycastle.crypto.InvalidCipherTextException;
import sg.edu.ntu.sce.sands.crypto.dcpabe.key.PersonalKey;
import sg.edu.ntu.sce.sands.crypto.dcpabe.ac.AccessStructure;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;

public class ABEUtil {

    public static void main(String args[]) {
        System.out.println("Ao encriptar, são salvos 2 arquivos na raiz do projeto: gp.json (GlobalParameters) e ak.json (AuthorityKeys).\n" +
                "Ambos serão carregados automáticamente para fazer a decriptação.\n\n");
        Scanner scNum = new Scanner(System.in);
        Scanner scText = new Scanner(System.in);
        System.out.println("Deseja encriptar (1) ou decriptar (2)?");
        boolean encrypt = scNum.nextInt() == 1;

        if(encrypt) {
            System.out.println("Digite os atributos em notação infixa (disponíveis: MEDIC, PATIENT, NURSE, STUDENT)");
            System.out.println("Valor padrão: or or MEDIC PATIENT and STUDENT NURSE");
            String policy = scText.nextLine();
            policy = policy.isEmpty() ? "or or MEDIC PATIENT and STUDENT NURSE" : policy;

            System.out.println("Digite o caminho do arquivo a ser encriptado (dentro da pasta files)");
            System.out.println("Valor padrão: files/ABE.pdf");
            System.out.print("files/");
            String clearFilePath = scText.nextLine();
            clearFilePath = clearFilePath.isEmpty() ? "files/ABE.pdf" : "files/" + clearFilePath;
            System.out.println("Digite o nome do arquivo de saída");
            System.out.println("Valor padrão: files/ABE-Ciphered.pdf");
            System.out.print("files/");
            String cipheredFilePath = scText.nextLine();
            cipheredFilePath = cipheredFilePath.isEmpty() ? "files/ABE-Ciphered.pdf" : "files/" + cipheredFilePath;
            encrypt(clearFilePath, cipheredFilePath, policy);
        }
        else {
            System.out.println("Digite os atributos separados por espaço");
            System.out.println("Valor padrão: STUDENT NURSE");
            String policy = scText.nextLine();
            policy = policy.isEmpty() ? "STUDENT NURSE" : policy;

            System.out.println("Digite o nome do arquivo de entrada");
            System.out.println("Valor padrão: files/ABE-Ciphered.pdf");
            System.out.print("files/");
            String cipheredFilePath = scText.nextLine();
            cipheredFilePath = cipheredFilePath.isEmpty() ? "files/ABE-Ciphered.pdf" : "files/" + cipheredFilePath;

            System.out.println("Digite o nome do arquivo de saída");
            System.out.println("Valor padrão: files/ABE-Deciphered.pdf");
            System.out.print("files/");
            String clearFilePath = scText.nextLine();
            clearFilePath = clearFilePath.isEmpty() ? "files/ABE-Deciphered.pdf" : "files/" + clearFilePath;

            decrypt(cipheredFilePath, clearFilePath, policy.split(" "));
        }
    }

    private static void encrypt(String clearFilePath, String cipheredFilePath, String policy){
        GlobalParameters gp = DCPABE.globalSetup(160);
        AuthorityKeys ak = DCPABE.authoritySetup("HOSPITAL X", gp, "MEDIC", "PATIENT", "NURSE", "STUDENT");

        Message m = DCPABE.generateRandomMessage(gp);

        PublicKeys pks = new PublicKeys();
        //for (String auth : authorities)
        pks.subscribeAuthority(ak.getPublicKeys());
        AccessStructure as = AccessStructure.buildFromPolicy(policy);
        Ciphertext ct = DCPABE.encrypt(m, as, gp, pks);

        try (
                FileOutputStream fos = new FileOutputStream(cipheredFilePath);
                ObjectOutputStream oos = new ObjectOutputStream(fos);

                FileInputStream fis = new FileInputStream(clearFilePath);
                BufferedInputStream bis = new BufferedInputStream(fis);
        ) {
            oos.writeObject(ct);

            PaddedBufferedBlockCipher aes = Utility.initializeAES(m.getM(), true);

            encryptOrDecryptPayload(aes, bis, oos);
            writeObjToJSON("gp.json", gp);
            writeObjToJSON("ak.json", ak);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void decrypt(String cipheredFilePath, String clearFilePath, String ...policies) {
        try (
                FileInputStream input = new FileInputStream(cipheredFilePath);
                ObjectInputStream oIn = new ObjectInputStream(input)
        ) {
            GlobalParameters gp = readObjFromJSON("gp.json", GlobalParameters.class);
            AuthorityKeys ak = readObjFromJSON("ak.json", AuthorityKeys.class);

            PersonalKeys perKeys = new PersonalKeys("Ramon");

            for(String policy : policies)
                perKeys.addKey(DCPABE.keyGen("Ramon", policy, ak.getSecretKeys().get(policy), gp));


            Ciphertext ct = Utility.readCiphertext(oIn);

            Message m = DCPABE.decrypt(ct, perKeys, gp);

            PaddedBufferedBlockCipher aes = Utility.initializeAES(m.getM(), false);

            try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(clearFilePath))) {
                encryptOrDecryptPayload(aes, oIn, bos);
                bos.flush();
            }
            //writeObjToJSON("RamonKeys.json", perKeys);
        } catch (IOException | ClassNotFoundException | DataLengthException | IllegalStateException | InvalidCipherTextException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            System.out.println("Atributos não satisfazem a política do texto cifrado.");
        }
    }

    private static void encryptOrDecryptPayload(PaddedBufferedBlockCipher cipher, InputStream is, OutputStream os) throws DataLengthException, IllegalStateException, InvalidCipherTextException, IOException {
        byte[] inBuff = new byte[cipher.getBlockSize()];
        byte[] outBuff = new byte[cipher.getOutputSize(inBuff.length)];
        int nbytes;
        while (-1 != (nbytes = is.read(inBuff, 0, inBuff.length))) {
            int length1 = cipher.processBytes(inBuff, 0, nbytes, outBuff, 0);
            os.write(outBuff, 0, length1);
        }
        nbytes = cipher.doFinal(outBuff, 0);
        os.write(outBuff, 0, nbytes);
    }

    private static <T extends Serializable> void writeObjToJSON (String fileName, T object){
        ObjectMapper mapper = new ObjectMapper();

        try {
            File file = new File(fileName);
            mapper.writeValue(file, object);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static <T extends Serializable> T readObjFromJSON(String fileName, Class<T> tClass){
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
}
