/*
SPDX-License-Identifier: Apache-2.0
*/

package org.example;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Network;
import org.hyperledger.fabric.gateway.Wallet;

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

		// create a gateway connection
		try (Gateway gateway = builder.connect()) {

			// get the network and contract
			Network network = gateway.getNetwork("mychannel");
			Contract contract = network.getContract("fabcar");

			byte[] result;

			/*
				final Context ctx, final String key, final String name, final String gpKey, final String... attributes
			 */
			//contract.submitTransaction("createAuthority", "AUT000", "HOSPITAL Fabric", "GP0000", "MEDIC", "CARDIOLOGIST", "PATIENT");

			//contract.submitTransaction("createGlobalParameter", "GP000");
			contract.submitTransaction("createCar", "CAR10", "VW", "Polo", "Grey", "Mary");

			//result = contract.evaluateTransaction("queryCar", "CAR10");
			result = contract.evaluateTransaction("queryCar", "CAR10");
			System.out.println(new String(result));
		}
	}

	private static String encodeFileToBase64Binary(String fileName) throws IOException {
		File file = new File(fileName);
		byte[] encoded = Base64.encodeBase64(FileUtils.readFileToByteArray(file));
		return new String(encoded, StandardCharsets.US_ASCII);
	}


}
