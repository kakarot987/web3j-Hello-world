package org.web3j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.web3j.generated.contracts.HelloWorld;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.ClientTransactionManager;
import org.web3j.tx.gas.StaticGasProvider;

class HelloWorldIntegrationTest {
    /** Requires disposable local Ganache, started by CI or the developer. */
    @Test
    @EnabledIfEnvironmentVariable(named = "RUN_GANACHE_TESTS", matches = "true")
    void deployReadUpdateAndRejectNonOwner() throws Exception {
        Web3j client = Web3j.build(new HttpService(
                System.getenv().getOrDefault("WEB3J_NODE_URL", "http://127.0.0.1:8545")));
        try {
            Web3App.requireLocalChain(client.ethChainId().send().getChainId());
            List<String> accounts = client.ethAccounts().send().getAccounts();
            assertTrue(accounts.size() >= 2, "Ganache needs at least two unlocked accounts");

            var gas = new StaticGasProvider(
                    BigInteger.valueOf(2_000_000_000L), BigInteger.valueOf(4_500_000L));
            var owner = new ClientTransactionManager(client, accounts.get(0));
            HelloWorld contract = HelloWorld.deploy(client, owner, gas, "Original").send();
            assertEquals("Original", contract.greeting().send());

            contract.newGreeting("Updated").send();
            assertEquals("Updated", contract.greeting().send());

            var anotherAccount = new ClientTransactionManager(client, accounts.get(1));
            HelloWorld nonOwnerView = HelloWorld.load(
                    contract.getContractAddress(), client, anotherAccount, gas);
            boolean rejected;
            try {
                rejected = !nonOwnerView.newGreeting("Unauthorized").send().isStatusOK();
            } catch (Exception expected) {
                // Nodes may surface an EVM revert as an RPC exception.
                rejected = true;
            }
            assertTrue(rejected, "Only the owner should update the greeting");
            assertEquals("Updated", contract.greeting().send());
        } finally {
            client.shutdown();
        }
    }
}
