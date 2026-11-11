package org.web3j;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigInteger;
import org.junit.jupiter.api.Test;

class Web3AppTest {
    @Test
    void acceptsOnlyKnownLocalChainIds() {
        assertDoesNotThrow(() -> Web3App.requireLocalChain(BigInteger.valueOf(1337)));
        assertDoesNotThrow(() -> Web3App.requireLocalChain(BigInteger.valueOf(5777)));
        assertDoesNotThrow(() -> Web3App.requireLocalChain(BigInteger.valueOf(31337)));
        assertThrows(IllegalStateException.class,
                () -> Web3App.requireLocalChain(BigInteger.ONE));
        assertThrows(IllegalStateException.class,
                () -> Web3App.requireLocalChain(null));
    }
}
