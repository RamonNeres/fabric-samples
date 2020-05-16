/*
SPDX-License-Identifier: Apache-2.0
*/

package org.example;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Network;
import org.hyperledger.fabric.gateway.Wallet;
import sg.edu.ntu.sce.sands.crypto.dcpabe.AuthorityKeys;
import sg.edu.ntu.sce.sands.crypto.dcpabe.DCPABE;
import sg.edu.ntu.sce.sands.crypto.dcpabe.GlobalParameters;
import sg.edu.ntu.sce.sands.crypto.dcpabe.key.PublicKey;

public class ClientApp {

	static {
		System.setProperty("org.hyperledger.fabric.sdk.service_discovery.as_localhost", "true");
	}

	public static void main(String[] args) throws Exception {
		// Load a file system based wallet for managing identities.
		Path walletPath = Paths.get("wallet");
		Wallet wallet = Wallet.createFileSystemWallet(walletPath);

		// load a CCP
		Path networkConfigPath = Paths.get("..", "..", "first-network", "connection-org1.yaml");

		Gateway.Builder builder = Gateway.createBuilder();
		builder.identity(wallet, "user3").networkConfig(networkConfigPath).discovery(true);

		final String GP_KEY = "GP000";
		final String AUTH_KEY = "AUT000";
		final String AUTHORITY_ID = "HOSPITAL X";
		final String POLICY1 = "MEDIC";
		final String POLICY2 = "CARDIOLOGIST";
		final String USER_KEY1 = "USR000";
		final String USER_ID1 = UUID.randomUUID().toString();
		final String USER1_ATTRIBUTE1 = "PATIENT";
		final String USER_KEY2 = "USR001";
		final String USER_ID2 = UUID.randomUUID().toString();
		final String USER2_ATTRIBUTE1 = "MEDIC";
		final String USER2_ATTRIBUTE2 = "CARDIOLOGIST";
		final String USER_KEY3 = "USR002";
		final String USER_ID3 = UUID.randomUUID().toString();
		final String USER3_ATTRIBUTE1 = "MEDIC";

		final String REC_KEY1 = "REC001";
		final String FOLDER = "";

		// create a gateway connection
		try (Gateway gateway = builder.connect()) {

			// get the network and contract
			Network network = gateway.getNetwork("mychannel");
			Contract contract = network.getContract("fabcar");

			byte[] result;

			System.out.println("Criando GlobalParameters");
			/* Criar GlobalParameter */
			GlobalParameters gp = DCPABE.globalSetup(160);
			String pairingParameter = new String(Base64.encodeBase64(Util.objToByteArray(gp.getPairingParameters())));
			String g1 = new String(Base64.encodeBase64(gp.getG1().toBytes()));
			contract.submitTransaction("createGlobalParameter", GP_KEY, pairingParameter, g1);

			result = contract.evaluateTransaction("queryGlobalParameter", GP_KEY);
			System.out.println(new String(result));
			System.out.println();

			/* Recupera GlobalParameter */
//			System.out.println("Recuperando GlobalParameters");
//			result = contract.evaluateTransaction("queryGlobalParameter", GP_KEY);
//			GlobalParameters gp = Util.readObjFromJSON(result, GlobalParameters.class);

			/* Cria nova autoridade */
			System.out.println("Criando autoridade");
			AuthorityKeys ak = DCPABE.authoritySetup(AUTHORITY_ID, gp, POLICY1);

			String eg1ai = new String(java.util.Base64.getEncoder().encode(ak.getPublicKeys().get(POLICY1).getEg1g1ai()));
			String g1yi = new String(java.util.Base64.getEncoder().encode(ak.getPublicKeys().get(POLICY1).getG1yi()));

			contract.submitTransaction("createAuthority", AUTH_KEY, AUTHORITY_ID, POLICY1, eg1ai, g1yi);

			result = contract.evaluateTransaction("queryAuthority", AUTH_KEY);
			System.out.println(new String(result));
			System.out.println();

			/* Salva ak para manter as chaves secretas */
			System.out.println("Salvando chaves secretas localmente...");
			Util.saveSecretKeys(ak);

			/* Cria novo ak para inserir novo atributo */
			System.out.println("Criando outra autoridade para gerar novo atributo...");
			AuthorityKeys ak2 = DCPABE.authoritySetup(AUTHORITY_ID, gp, POLICY2);

			/* Salva ak para manter as chaves secretas */
			System.out.println("Salvando novas chaves secretas...");
			Util.saveSecretKeys(ak2);

			PublicKey pk = ak2.getPublicKeys().get(POLICY2);
			System.out.println("Adicionando novo atributo...");
			contract.submitTransaction("addPublicKey", AUTH_KEY, POLICY2, Util.getBase64Eg1g1ai(pk.getEg1g1ai()),
					Util.getBase64G1yi(pk.getG1yi()));

			result = contract.evaluateTransaction("queryAuthority", AUTH_KEY);
			System.out.println(new String(result));
			System.out.println();

			//TODO: Salvar SecretKeys referentes as PublicKeys adicionadas
			System.out.println("Criando usuários...");
			contract.submitTransaction(USER_KEY1, USER_ID1, USER1_ATTRIBUTE1);
			contract.submitTransaction(USER_KEY2, USER_ID2, USER2_ATTRIBUTE1 + "," + USER2_ATTRIBUTE2);
			contract.submitTransaction(USER_KEY3, USER_ID3, USER3_ATTRIBUTE1);

			result = contract.evaluateTransaction("queryUser", USER_KEY1);
			System.out.println(new String(result));
			result = contract.evaluateTransaction("queryUser", USER_KEY2);
			System.out.println(new String(result));
			result = contract.evaluateTransaction("queryUser", USER_KEY3);
			System.out.println(new String(result));
			System.out.println();

			//TODO: Encriptar o pdf
			//System.out.println("Encriptando PDF...");

			System.out.println("Enviando PDF...");
			contract.submitTransaction(REC_KEY1, USER_ID1, FOLDER, AUTH_KEY, GP_KEY,
					Util.encodeFileToBase64Binary("DecentralizingAttributeBasedE.pdf"));

			result = contract.evaluateTransaction("queryMedRecord", REC_KEY1);
			System.out.println(new String(result));
			System.out.println();
			result = contract.evaluateTransaction("queryMedRecordFile", REC_KEY1);
			System.out.println(new String(result));
		}
	}

	private static String encodeFileToBase64Binary(String fileName) throws IOException {
		File file = new File(fileName);
		byte[] encoded = Base64.encodeBase64(FileUtils.readFileToByteArray(file));
		return new String(encoded, StandardCharsets.US_ASCII);
	}


}
