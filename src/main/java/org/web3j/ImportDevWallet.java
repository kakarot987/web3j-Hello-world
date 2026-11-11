package org.web3j;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.WalletUtils;

/** Imports a disposable Ganache account into a Git-ignored JSON keystore. */
public final class ImportDevWallet {
    private ImportDevWallet() { }

    public static void main(String[] args) throws Exception {
        String password = required("WEB3J_WALLET_PASSWORD");
        String privateKey = required("DEV_WALLET_PRIVATE_KEY").replaceFirst("^0[xX]", "");
        if (!privateKey.matches("(?i)[0-9a-f]{64}")) {
            throw new IllegalArgumentException("DEV_WALLET_PRIVATE_KEY must be a 32-byte hex key.");
        }

        Path directory = Path.of(System.getenv().getOrDefault("WEB3J_WALLET_DIR", "keystore"))
                .toAbsolutePath().normalize();
        Files.createDirectories(directory);

        String fileName = WalletUtils.generateWalletFile(
                password,
                ECKeyPair.create(new BigInteger(privateKey, 16)),
                directory.toFile(),
                true);
        Path wallet = directory.resolve(fileName);
        try {
            Files.setPosixFilePermissions(wallet,
                    Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
        } catch (UnsupportedOperationException ignored) {
            // POSIX file permissions are not available on Windows.
        }

        System.out.println("Encrypted wallet created. Set WEB3J_WALLET_PATH=" + wallet);
        System.out.println("Unset DEV_WALLET_PRIVATE_KEY immediately after importing.");
    }

    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " is required.");
        }
        return value;
    }
}
