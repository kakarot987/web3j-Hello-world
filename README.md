# Web3j Hello World — Local Blockchain Demo

A **learning-only** Java 21 + Web3j project. It deploys a Solidity greeting contract to a local Ganache Ethereum chain, reads the initial greeting, updates it as owner, and tests access control.

**Never use this application with real funds or production credentials.** The application refuses to deploy to any chain except the local development chain IDs 1337, 5777, and 31337.

## Requirements

- Java 21 (Gradle 8.7 wrapper included)
- Node.js/npm or an existing Ganache installation
- Docker, optionally

No global Gradle or Web3j CLI installation is needed.

## Project structure

```text
src/main/java/org/web3j/Web3App.java          # Deploy, read and update
src/main/java/org/web3j/ImportDevWallet.java   # Make an encrypted development keystore
src/main/solidity/HelloWorld.sol               # Greeting with owner-only updates
src/test/java/org/web3j/                      # Unit and local-chain integration tests
build.gradle                                   # Java, Gradle and Solidity compilation
Dockerfile                                     # JDK builder and JRE runtime
.github/workflows/ci.yml                      # Continuous integration
```

## Quick start

### 1. Start Ganache

Start a fresh, disposable local chain in one terminal:

```bash
npx --yes ganache@7.9.2 --server.host 127.0.0.1 --server.port 8545 --chain.chainId 1337
```

Ganache generates test accounts with **fictitious development ETH**. Use only new disposable accounts; never reuse the key previously committed to this repository.

### 2. Import a new development account

In a second terminal, from the project root (Linux/macOS Bash):

```bash
read -rs -p 'New Ganache development private key: ' DEV_WALLET_PRIVATE_KEY; echo
export DEV_WALLET_PRIVATE_KEY
read -rs -p 'New local wallet password: ' WEB3J_WALLET_PASSWORD; echo
export WEB3J_WALLET_PASSWORD

bash ./gradlew importDevWallet
unset DEV_WALLET_PRIVATE_KEY
```

The importer prints the path of an encrypted JSON keystore. Copy that **new** path into the second terminal:

```bash
export WEB3J_WALLET_PATH='/absolute/path/printed/by/importer.json'
export WEB3J_NODE_URL='http://127.0.0.1:8545'
```

The password and key remain sensitive even in environment variables; these steps are for disposable local development accounts only. The keystore directory is ignored by Git and excluded from Docker builds.

### 3. Build and run

```bash
bash ./gradlew clean build
bash ./gradlew run
```

You should see the contract address, its original greeting and the new greeting. The build compiles the Solidity source and generates the Java Web3j contract wrapper.

## Tests

Unit tests run with a normal build. To run the deployment and access-control integration test against your running Ganache instance:

```bash
RUN_GANACHE_TESTS=true bash ./gradlew test
```

The integration test uses Ganache's first two unlocked accounts; it requires no repository-stored private keys. GitHub Actions launches a disposable Ganache node and runs the integration test on each push and pull request.

## Docker

The Docker build uses a Java 21 **JDK** to compile and a Java 21 **JRE** to execute the result. Secrets and keystores are excluded by `.dockerignore`.

```bash
docker build -t web3app .
```

Example Docker run on **Linux**, with Ganache on the host and a wallet created above:

```bash
WALLET_FILE="$(basename "$WEB3J_WALLET_PATH")"
docker run --rm --network host --user "$(id -u):$(id -g)" \
  -e WEB3J_NODE_URL='http://127.0.0.1:8545' \
  -e WEB3J_WALLET_PATH="/wallet/$WALLET_FILE" \
  -e WEB3J_WALLET_PASSWORD \
  -v "$PWD/keystore:/wallet:ro" \
  web3app
```

The container uses a read-only wallet mount; matching the host user lets it read the wallet's restrictive file permissions. Docker Desktop may require different network settings. Never publish the Ganache RPC endpoint to an untrusted network.

## Configuration

| Environment variable | Required | Description |
| --- | --- | --- |
| `WEB3J_NODE_URL` | No | RPC URL; default `http://127.0.0.1:8545` |
| `WEB3J_WALLET_PATH` | Yes, for app | Existing encrypted development keystore |
| `WEB3J_WALLET_PASSWORD` | Yes, for app/import | Password for the development keystore |
| `DEV_WALLET_PRIVATE_KEY` | Only during import | New, disposable Ganache test key; unset after import |
| `WEB3J_WALLET_DIR` | No | Keystore output directory; default `keystore/` |
| `RUN_GANACHE_TESTS` | No | Set to `true` to run integration tests |

The Solidity contract allows the owner to update its greeting. The old legacy `selfdestruct` behavior has been removed.

## Security notice

The original public repository included a literal private key and a JSON keystore. **Consider the published account permanently compromised, even after these files have been deleted.** Never deploy using it again. If it controls real assets, move them to a newly generated secure account.

This change untracks the old wallet file and removes IDE and generated Node files. The current source tree no longer includes them. **Git history still contains old commits with the credentials.** Deleting a file from a new commit does not delete it from historical commits; rotate or retire exposed credentials regardless of history cleanup.

Do not commit private keys, keystores, password files or real wallets. This application is a **learning example**, not production Ethereum software.

## License

Apache-2.0. See [LICENSE](LICENSE).
