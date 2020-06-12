/*
SPDX-License-Identifier: Apache-2.0
*/

package org.example;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLOutput;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.google.errorprone.annotations.DoNotCall;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hyperledger.fabric.gateway.*;
import sg.edu.ntu.sce.sands.crypto.dcpabe.AuthorityKeys;
import sg.edu.ntu.sce.sands.crypto.dcpabe.DCPABE;
import sg.edu.ntu.sce.sands.crypto.dcpabe.GlobalParameters;
import sg.edu.ntu.sce.sands.crypto.dcpabe.PersonalKeys;
import sg.edu.ntu.sce.sands.crypto.dcpabe.key.PublicKey;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import sg.edu.ntu.sce.sands.crypto.dcpabe.key.SecretKey;

public class ClientApp {

    static {
        System.setProperty("org.hyperledger.fabric.sdk.service_discovery.as_localhost", "true");
    }

    /* AWS */
    private static final String BUCKET_NAME = "medical-records-pgc";
    private static final Regions clientRegion = Regions.SA_EAST_1;
    private static final AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
            .withRegion(clientRegion)
            .build();
    /* AWS */
    //Todo: Adicionar politicas no ledger
    //Todo: Trazer todos os registros por usuario
    private  static void handleUserChoice(Contract contract) {
        try {
            int choice;
            String input;
            Scanner numReader = new Scanner(System.in);
            Scanner txtReader = new Scanner(System.in);

            do {
                System.out.println(" 1 - Criar GlobalParameters");
                System.out.println(" 2 - Criar Authority");
                System.out.println(" 3 - Criar User");
                System.out.println(" 4 - Enviar MedRecord");
                System.out.println(" 5 - Query GlobalParameters");
                System.out.println(" 6 - Query Authority");
                System.out.println(" 7 - Query User");
                System.out.println(" 8 - Query MedRecord");
                System.out.println(" 9 - Query MedRecordFile");
                System.out.println("10 - Adicionar atributo a Authority");
                //System.out.println("10 - Gerar chave secreta para usuário");
                System.out.println("0 - Sair");
                choice = numReader.nextInt();

                switch (choice) {
                    case 1:
                        System.out.println("Digite a chave do GP");
                        String gpCreatekey = txtReader.nextLine();
                        createGP(contract, gpCreatekey);
                        System.out.println("Sucesso");
                        break;
                    case 2:
                        System.out.println("Digite a chave da Authority");
                        String authKeyAuthCreate = txtReader.nextLine();
                        System.out.println("Digite o ID da autoridade");
                        String authIDAuthCreate = txtReader.nextLine();
                        System.out.println("Digite a chave do gp a ser utilziado na criação");
                        String gpKeyAuthCreate = txtReader.nextLine();
                        GlobalParameters gpAuthCreate = getGP(contract, gpKeyAuthCreate);
                        System.out.println("Digite o atributo inicial da autoridade");
                        String attrAuthCreate = txtReader.nextLine();
                        AuthorityKeys akAuthCreate =
                                createAuthority(contract, authKeyAuthCreate, authIDAuthCreate,gpAuthCreate, attrAuthCreate);
                        Util.saveSecretKeys(akAuthCreate);
                        System.out.println("Sucesso");
                        break;
                    case 3:
                        System.out.println("Digite o id do usuário");
                        String userIDUserCreate = txtReader.nextLine();
                        System.out.println("Digite os atributos do usuário (separados por espaço)");
                        String userAttributesUserCreate = txtReader.nextLine();
                        createUser(contract, userIDUserCreate, userIDUserCreate, userAttributesUserCreate.split(" "));
                        System.out.println("Success");
                        break;
                    case 4:
                        System.out.println("Digite o caminho do arquivo");
                        String filePathMedRecordCreate = txtReader.nextLine();
                        byte[] clearFile = FileUtils.readFileToByteArray(new File(filePathMedRecordCreate));
                        System.out.println("Digite o id do usuário que está enviando o arquivo");
                        String userIDMedRecordCreate = txtReader.nextLine();
                        User userMedRecordCreate = getUser(contract, userIDMedRecordCreate);
                        System.out.println("Digite o id do GlobalParameter");
                        String gpIDMedRecordCreate = txtReader.nextLine();
                        GlobalParameters gp = getGP(contract, gpIDMedRecordCreate);
                        System.out.println("Digite a policy (notação infixa)");
                        String policyMedRecordCreate = txtReader.nextLine();
                        System.out.println("Digite o id da autoridade");
                        String authId = txtReader.nextLine();
                        Authority auth = getAuthority(contract, authId);

                        MedRecord md = createMedRecord(contract, clearFile, userMedRecordCreate, policyMedRecordCreate, gp, auth, gpIDMedRecordCreate);
                        System.out.println("Nome do arquivo no S3: " + md.getFileName());
                        break;
                    case 5:
                        System.out.println("Digite o id do GlobalParameter");
                        String gpIDQueryGP = txtReader.nextLine();
                        String gpJson = Util.objToJson(getGP(contract, gpIDQueryGP));
                        System.out.println(gpJson);
                        break;
                    case 6:
                        System.out.println("Digite o ID da Authority");
                        String authIdQueryAuth = txtReader.nextLine();
                        String authJson = Util.objToJson(getAuthority(contract, authIdQueryAuth));
                        System.out.println(authJson);
                        break;
                    case 7:
                        System.out.println("Digite o ID do usuário");
                        input = txtReader.nextLine();
                        String userJson = Util.objToJson(getUser(contract, input));
                        System.out.println(userJson);
                        break;
                    case 8:
                        System.out.println("Digite o ID do MedRecord");
                        input = txtReader.nextLine();
                        String medRecordJson = Util.objToJson(getMedRecord(contract, input));
                        System.out.println(medRecordJson);
                        break;
                    case 9:
                        System.out.println("Digite o ID do MedRecord");
                        String medRecordID = txtReader.nextLine();
                        System.out.println("Digite o ID do usuário");
                        String userId = txtReader.nextLine();
                        System.out.println("Digite o ID do gp");
                        String gpId = txtReader.nextLine();
                        System.out.println("Digite o caminho do arquivo decriptado");
                        String filePath = txtReader.nextLine();
                        String medRecFileJson = Util.objToJson(getMedRecordFile(contract, medRecordID, gpId, userId, filePath));
                        System.out.println(medRecFileJson);
                        break;
                    case 10:
                        System.out.println("Digite o ID da Authority");
                        String authID = txtReader.nextLine();
                        System.out.println("Digite o ID do GP");
                        String gpId10 = txtReader.nextLine();
                        System.out.println("Digite o novo atributo");
                        String att10 = txtReader.nextLine();
                        String pkJson = Util.objToJson(addAttribute(contract, authID, gpId10, att10));
                        System.out.println(pkJson);
                        break;
                        //Todo: gerar e salvar chaves secretas do usuário
//                    case 11:
//                        System.out.println("Digite o ID do usuário");
//                        input = txtReader.nextLine();
//                        User user = getUser(contract, input);
//                        //Todo: verificar possibilidade de múltiplas
//                        System.out.println("Digite o id da autoridade");
//                        input = txtReader.nextLine();
//                        Authority auth = getAuthority(contract, input);
//                        System.out.println("Digite atributos do usuário (separados por espaço)");
//                        input = txtReader.nextLine();
//                        Util.generateUserSecretKeys(user.getId(), auth, input.split(" "));

                }

            } while (choice != 0);
        } catch(IOException | ContractException | TimeoutException | InterruptedException | NoSuchAlgorithmException |
                InvalidCipherTextException | ClassNotFoundException e) {
            System.out.println("Erro: ");
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        Gateway gateway = null;
        try {
            // Load a file system based wallet for managing identities.
            Path walletPath = Paths.get("wallet");
            Wallet wallet = Wallet.createFileSystemWallet(walletPath);

            // load a CCP
            Path networkConfigPath = Paths.get("..", "..", "first-network", "connection-org1.yaml");

            Gateway.Builder builder = Gateway.createBuilder();
            builder.identity(wallet, "user1").networkConfig(networkConfigPath).discovery(true);
            gateway = builder.connect();
            // get the network and contract
            Network network = gateway.getNetwork("mychannel");
            Contract contract = network.getContract("fabcar");

            handleUserChoice(contract);
        } catch (IOException e) {
            System.out.println("Erro: ");
            System.out.println(e.getMessage());
        }
        finally {
            assert gateway != null;
            gateway.close();
        }
    }

    public static void main1(String[] args) throws Exception {
        // Load a file system based wallet for managing identities.
        Path walletPath = Paths.get("wallet");
        Wallet wallet = Wallet.createFileSystemWallet(walletPath);

        // load a CCP
        Path networkConfigPath = Paths.get("..", "..", "first-network", "connection-org1.yaml");

        Gateway.Builder builder = Gateway.createBuilder();
        builder.identity(wallet, "user1").networkConfig(networkConfigPath).discovery(true);


        final String GP_KEY = "GP014";
        final String AUTH_KEY = "AUT014";
        final String AUTHORITY_ID = "HOSPITAL Y";
        final String ATTRIBUTE1 = "MEDIC";
        final String ATTRIBUTE2 = "CARDIOLOGIST";
        final String USER_KEY1 = "USR0040";
        final String USER_ID1 = UUID.randomUUID().toString();
        final String USER1_ATTRIBUTE1 = "PATIENT";
        final String USER_KEY2 = "USR0041";
        final String USER_ID2 = UUID.randomUUID().toString();
        final String USER2_ATTRIBUTE1 = "MEDIC";
        final String USER2_ATTRIBUTE2 = "CARDIOLOGIST";
        final String USER_KEY3 = "USR0042";
        final String USER_ID3 = UUID.randomUUID().toString();
        final String USER3_ATTRIBUTE1 = "MEDIC";

        final String REC_KEY1 = "REC017";

        // create a gateway connection
        try (Gateway gateway = builder.connect()) {

            // get the network and contract
            Network network = gateway.getNetwork("mychannel");
            Contract contract = network.getContract("fabcar");

            System.out.println("Criando GlobalParameters...");
            /* Criar GlobalParameter */
            GlobalParameters gpOriginal = createGP(contract, GP_KEY);
            System.out.println();

            /* Recupera GlobalParameter */
            System.out.println("Recuperando GlobalParameters salvo no blockchain...");
            GlobalParameters gp = getGP(contract, GP_KEY);
            System.out.println("gp - " + gp);
            System.out.println();

            /* Cria nova autoridade */
            System.out.println("Criando autoridade...");
            AuthorityKeys ak = createAuthority(contract, AUTH_KEY, AUTHORITY_ID, gp, ATTRIBUTE1);
            System.out.println();

            /* Salva ak para manter as chaves secretas */
            System.out.println("Salvando chaves secretas localmente...");
            Util.saveSecretKeys(ak);
            System.out.println();

            /* Recupera autoridade */
            Authority authority = getAuthority(contract, AUTH_KEY);
            System.out.println(authority.getAuthorityID());
            authority.getPublicKeys().forEach((att, pk) -> {
                System.out.println("att: " + att + "- pk: " + pk);
            });

            /* Cria novo ak para inserir novo atributo */
            System.out.println("Criando outra autoridade para gerar novo atributo...");
            AuthorityKeys akAux = addAttribute(contract, AUTH_KEY, authority, gp, ATTRIBUTE2);
            System.out.println();
            Authority authorityWithNewAtt = getAuthority(contract, AUTH_KEY);
            System.out.println(authorityWithNewAtt.getAuthorityID());
            authorityWithNewAtt.getPublicKeys().forEach((att, pk) -> {
                System.out.println("att: " + att + "- pk: " + pk);
            });

            /* Salva ak para manter as chaves secretas */
            System.out.println("Salvando novas chaves secretas...");
            Util.saveSecretKeys(akAux);
            System.out.println();

            System.out.println("Criando usuários...");
            createUser(contract, USER_KEY1, USER_ID1, USER1_ATTRIBUTE1);
            createUser(contract, USER_KEY2, USER_ID2, USER2_ATTRIBUTE1, USER2_ATTRIBUTE2);
            createUser(contract, USER_KEY3, USER_ID3, USER3_ATTRIBUTE1);

            User user1 = getUser(contract, USER_KEY1);
            System.out.println(user1.getId() + " " + String.join(", ", user1.getAttributes()));
            User user2 = getUser(contract, USER_KEY2);
            System.out.println(user2.getId() + " " + String.join(", ", user2.getAttributes()));
            User user3 = getUser(contract, USER_KEY3);
            System.out.println(user3.getId() + " " + String.join(", ", user3.getAttributes()));
            System.out.println();

            System.out.println("Encriptando PDF...");
            byte[] clearPdf = FileUtils.readFileToByteArray(new File("DCPABE.pdf"));
            byte[] cipheredPdf = ABEUtil.encrypt(clearPdf, "and " + USER2_ATTRIBUTE1 + " " + USER2_ATTRIBUTE2, gp, authorityWithNewAtt);
            System.out.println();

            System.out.println("Calculando hash do arquivo criptografado...");
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(cipheredPdf);
            String encodedHash = Util.encodeBytesToBase64(hash);
            System.out.println();

            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            String fileName = Long.toString(timestamp.getTime());
            //Todo: verificar necessidade/possibilidade de transação
            //Todo: mudar nome do campo bucket para fileName
            System.out.println("Enviando Informações do arquivo para o chaincode...");
            createMedRecord(contract, REC_KEY1, USER_ID1, fileName, encodedHash, AUTH_KEY, GP_KEY);
            System.out.println();

            System.out.println("Enviando arquivo para o S3...");
            sendRecordToS3(cipheredPdf, user1.getId(), fileName);
            System.out.println();

            System.out.println("Obtendo dados do arquivo no blockchain...");
            MedRecord medRecord = getMedRecord(contract, REC_KEY1);
            System.out.println(medRecord.getUserId() + " - " + medRecord.getHash() + " - " + medRecord.getFileName());
            System.out.println();

            System.out.println("Baixando arquivo do S3...");
            byte[] cypheredPdf = getRecordFromS3(user1.getId(), medRecord.getFileName());
            System.out.println();

            //Todo: chave secreta do usuário deveria ser salva
            System.out.println("Gerando chave secreta do usuário e decriptando arquivo...");
            PersonalKeys perKeys = new PersonalKeys(user2.getId());

            for (String attribute : user2.getAttributes()) {
                SecretKey sk = Util.readAuthoritySecretKey(authority.getAuthorityID(), attribute);
                Util.writeObjToJSON("sk.json", sk);
                perKeys.addKey(DCPABE.keyGen(user2.getId(), attribute, sk, gp));
            }
            System.out.println();

            byte[] decryptedPdf = ABEUtil.decrypt(cypheredPdf, perKeys, gp, user2.getAttributes().toArray(new String[]{}));

            FileUtils.writeByteArrayToFile(new File("decPdf.pdf"), decryptedPdf);
        }
    }

    private static AuthorityKeys createAuthority(Contract contract, String authKey, String authorityID, GlobalParameters gp, String attribute)
            throws ContractException, TimeoutException, InterruptedException {
        AuthorityKeys ak = DCPABE.authoritySetup(authorityID, gp, attribute);

        String eg1ai = new String(java.util.Base64.getEncoder().encode(ak.getPublicKeys().get(attribute).getEg1g1ai()));
        String g1yi = new String(java.util.Base64.getEncoder().encode(ak.getPublicKeys().get(attribute).getG1yi()));

        contract.submitTransaction("createAuthority", authKey, authorityID, attribute, eg1ai, g1yi);
        return ak;
    }

    private static GlobalParameters createGP(Contract contract, String key)
            throws IOException, ContractException, TimeoutException, InterruptedException {
        GlobalParameters gp = DCPABE.globalSetup(160);
        String pairingParameter = new String(Base64.encodeBase64(Util.objToByteArray(gp.getPairingParameters())));
        String g1 = new String(Base64.encodeBase64(gp.getG1().toBytes()));
        contract.submitTransaction("createGlobalParameter", key, pairingParameter, g1);
        return gp;
    }

    private static User createUser(Contract contract, String key, String userID, String... attributes)
            throws ContractException, TimeoutException, InterruptedException {
        User user = new User(userID, attributes);
        contract.submitTransaction("createUser", key, userID, String.join(",", attributes));
        return user;
    }

    @DoNotCall
    private static MedRecord createMedRecord(Contract contract, String key, String userID, String bucket, String hash, String authID, String gpKey)
            throws ContractException, TimeoutException, InterruptedException {
        MedRecord medRecord = new MedRecord(userID, bucket, hash, authID, gpKey);
        contract.submitTransaction("createMedRecord", key, userID, bucket, hash, String.join(",", authID), gpKey);
        return medRecord;
    }

    private static PublicKey addAttribute(Contract contract, String authKey, String gpId, String attribute)
            throws ContractException, TimeoutException, InterruptedException {
        Authority authority = getAuthority(contract, authKey);
        GlobalParameters gp = getGP(contract, gpId);
        AuthorityKeys ak = DCPABE.authoritySetup(authority.getAuthorityID(), gp, attribute);

        PublicKey pk = ak.getPublicKeys().get(attribute);

        Util.saveSecretKeys(ak);

        contract.submitTransaction("addPublicKey", authKey, attribute, Util.getBase64Eg1g1ai(pk.getEg1g1ai()),
                Util.getBase64G1yi(pk.getG1yi()));

        return pk;

    }

    private static AuthorityKeys addAttribute(Contract contract, String authKey, Authority authority, GlobalParameters gp, String attribute)
            throws ContractException, TimeoutException, InterruptedException {
        AuthorityKeys ak = DCPABE.authoritySetup(authority.getAuthorityID(), gp, attribute);

        PublicKey pk = ak.getPublicKeys().get(attribute);
        System.out.println("Adicionando novo atributo...");
        contract.submitTransaction("addPublicKey", authKey, attribute, Util.getBase64Eg1g1ai(pk.getEg1g1ai()),
                Util.getBase64G1yi(pk.getG1yi()));

        return ak;
    }

    private static GlobalParameters getGP(Contract contract, String key) throws ContractException {
        byte[] result = contract.evaluateTransaction("queryGlobalParameter", key);
        return Util.readObjFromJSON(result, GlobalParameters.class);
    }

    private static Authority getAuthority(Contract contract, String key) throws ContractException {
        byte[] result = contract.evaluateTransaction("queryAuthority", key);
        return Util.readObjFromJSON(result, Authority.class);
    }

    private static User getUser(Contract contract, String key) throws ContractException {
        byte[] result = contract.evaluateTransaction("queryUser", key);
        return Util.readObjFromJSON(result, User.class);
    }

    private static MedRecord getMedRecord(Contract contract, String key) throws ContractException {
        byte[] result = contract.evaluateTransaction("queryMedRecord", key);
        return Util.readObjFromJSON(result, MedRecord.class);
    }

    private static void sendRecordToS3(byte[] data, String userID, String fileName) {
        InputStream is = new ByteArrayInputStream(data);
        // Upload a file as a new object with ContentType and title specified.
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("application/pdf");
        metadata.addUserMetadata("title", "someTitle");
        PutObjectRequest request = new PutObjectRequest(BUCKET_NAME, userID + "/" + fileName, is, metadata);
        s3Client.putObject(request);
    }

    private static byte[] getRecordFromS3(String userID, String fileName) throws IOException {
        S3Object s3object = s3Client.getObject(BUCKET_NAME, userID + "/" + fileName);
        S3ObjectInputStream inputStream = s3object.getObjectContent();
        return IOUtils.toByteArray(inputStream);
    }

    private static MedRecord createMedRecord(Contract contract, byte[] clearFile, User user, String policy, GlobalParameters gp,
        Authority authority, String gpId) throws InterruptedException, InvalidCipherTextException, IOException, TimeoutException,
            ContractException, NoSuchAlgorithmException {

        byte[] encPdf = ABEUtil.encrypt(clearFile, policy, gp, authority);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(encPdf);
        String encodedHash = Util.encodeBytesToBase64(hash);
        System.out.println();

        String fileName = Long.toString(timestamp.getTime());
        sendRecordToS3(encPdf, user.getId(), fileName);

        //Todo: criar modelo para o gp para incluir chave
        return createMedRecord(contract, fileName, user.getId(), fileName, encodedHash, authority.getAuthorityID(), gpId);
    }

    private static MedRecord getMedRecordFile(Contract contract, String medRecordID, String gpID, String userId, String filePath)
            throws ContractException, IOException, InvalidCipherTextException, ClassNotFoundException {
        MedRecord mr = getMedRecord(contract, medRecordID);
        User user = getUser(contract, userId);
        GlobalParameters gp = getGP(contract, gpID);

        byte[] cypheredPdf = getRecordFromS3(mr.getUserId(), mr.getFileName());
        PersonalKeys perKeys = new PersonalKeys(user.getId());

        for (String attribute : user.getAttributes()) {
            SecretKey sk = Util.readAuthoritySecretKey(mr.getAuthorityID(), attribute);
            perKeys.addKey(DCPABE.keyGen(user.getId(), attribute, sk, gp));
        }

        byte[] decryptedPdf = ABEUtil.decrypt(cypheredPdf, perKeys, gp, user.getAttributes().toArray(new String[]{}));

        FileUtils.writeByteArrayToFile(new File(filePath), decryptedPdf);
        return mr;
    }
}
