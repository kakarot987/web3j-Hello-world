package org.web3j;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.generated.contracts.HelloWorld;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.StaticGasProvider;

/** Educational local-chain demo. Do not deploy real funds or production wallets. */
public final class Web3App {
    private static final Set<BigInteger> LOCAL_CHAIN_IDS = Set.of(
            BigInteger.valueOf(1337),
            BigInteger.valueOf(5777),
            BigInteger.valueOf(31337));
    private static final BigInteger GAS_PRICE = BigInteger.valueOf(2_000_000_000L);
    private static final BigInteger GAS_LIMIT = BigInteger.valueOf(4_500_000L);

    private Web3App() { }

    public static void main(String[] args) throws Exception {
        String walletPath = requiredEnvironmentVariable("WEB3J_WALLET_PATH");
        String walletPassword = requiredEnvironmentVariable("WEB3J_WALLET_PASSWORD");
        String rpcUrl = System.getenv().getOrDefault("WEB3J_NODE_URL", "http://127.0.0.1:8545");

        if (!Files.isRegularFile(Path.of(walletPath))) {
            throw new IllegalStateException("WEB3J_WALLET_PATH must point to an existing wallet file.");
        }
        if (!rpcUrl.startsWith("http://") && !rpcUrl.startsWith("https://")) {
            throw new IllegalStateException("WEB3J_NODE_URL must be an HTTP(S) RPC URL.");
        }

        Web3j web3j = Web3j.build(new HttpService(rpcUrl));
        try {
            requireLocalChain(web3j.ethChainId().send().getChainId());

            Credentials credentials;
            try {
                credentials = WalletUtils.loadCredentials(walletPassword, walletPath);
            } catch (Exception ignored) {
                throw new IllegalStateException(
                        "Cannot load development wallet. Check WEB3J_WALLET_PATH and WEB3J_WALLET_PASSWORD.");
            }

            var gasProvider = new StaticGasProvider(GAS_PRICE, GAS_LIMIT);
            System.out.println("Deploying HelloWorld to the local development chain...");
            HelloWorld contract = HelloWorld.deploy(
                    web3j, credentials, gasProvider, "Hello Blockchain World!").send();
            System.out.println("Contract address: " + contract.getContractAddress());
            System.out.println("Initial greeting: " + contract.greeting().send());

            contract.newGreeting("Updated through Web3j!").send();
            System.out.println("Updated greeting: " + contract.greeting().send());
        } finally {
            web3j.shutdown();
        }
    }

    static void requireLocalChain(BigInteger chainId) {
        if (chainId == null || !LOCAL_CHAIN_IDS.contains(chainId)) {
            throw new IllegalStateException(
                    "Refusing deployment: only local development chains 1337, 5777 and 31337 are allowed.");
        }
    }

    private static String requiredEnvironmentVariable(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " is required. See README.md for setup.");
        }
        return value;
    }
}
