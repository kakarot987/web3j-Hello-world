# Web3j HelloWorld Smart Contract Project

This project demonstrates how to deploy and interact with a simple Solidity smart contract (`HelloWorld`) using Java and the Web3j library. The contract is deployed to a local Ethereum blockchain simulated by Ganache.

---

## 📦 Project Overview

* **Smart Contract**: `HelloWorld.sol` written in Solidity.
* **Client**: Java application using Web3j.
* **Network**: Ganache (local Ethereum blockchain).
* **Tooling**: Gradle, Java 21, Web3j, Ganache UI.

---

## 🔧 Prerequisites

* Java 17+ (tested with Java 21)
* Gradle
* Ganache (GUI or CLI)
* Web3j CLI (optional, for code generation)

---

## 📟 Project Structure

```
.
├── contracts/
│   └── HelloWorld.sol
├── src/
│   └── main/
│       ├── java/
│       │   └── org/web3j/Web3App.java
│       └── resources/
├── build.gradle
└── README.md
```

---

## 📜 Smart Contract - HelloWorld.sol

```solidity
// SPDX-License-Identifier: Apache-2.0
pragma solidity ^0.7.0;

contract Mortal {
    address owner;
    constructor () { owner = msg.sender; }

    modifier onlyOwner {
        require(msg.sender == owner, "Only owner can call this function.");
        _;
    }

    function kill() public onlyOwner {
        selfdestruct(msg.sender);
    }
}

contract HelloWorld is Mortal {
    string greet;

    constructor (string memory _greet) {
        greet = _greet;
    }

    function newGreeting(string memory _greet) public onlyOwner {
        emit Modified(greet, _greet, greet, _greet);
        greet = _greet;
    }

    function greeting() public view returns (string memory) {
        return greet;
    }

    event Modified(
        string indexed oldGreetingIdx,
        string indexed newGreetingIdx,
        string oldGreeting,
        string newGreeting
    );
}
```

---

## ⚙️ Environment Variables

Set the following environment variables or hardcode them for testing:

| Variable                | Description                                              |
| ----------------------- | -------------------------------------------------------- |
| `WEB3J_NODE_URL`        | Ethereum RPC URL (e.g., Ganache `http://127.0.0.1:7545`) |
| `WEB3J_WALLET_PASSWORD` | Password for the wallet file (if using keystore)         |
| `WEB3J_WALLET_PATH`     | Path to your wallet file                                 |

Alternatively, you can use a private key directly (for Ganache testing accounts):

```java
Credentials credentials = Credentials.create("<private_key>");
```

---

## 🚀 Running the Project

### 1. Start Ganache

Ensure Ganache is running and that the selected account has ETH.

### 2. Compile and Run the Java App

```bash
./gradlew run
```

---

## 💠 Custom Gas Settings

To avoid block gas limit or cost issues, use:

```java
BigInteger gasPrice = BigInteger.valueOf(20_000_000_000L); // 20 Gwei
BigInteger gasLimit = BigInteger.valueOf(4_500_000);       // safe for Ganache

StaticGasProvider gasProvider = new StaticGasProvider(gasPrice, gasLimit);
HelloWorld helloWorld = HelloWorld.deploy(web3j, credentials, gasProvider, "Hello Blockchain World!").send();
```

---

## 🥪 Troubleshooting

* **`exceeds block gas limit`**: Lower the `gasLimit` value.
* **`insufficient funds for gas * price + value`**: Ensure the address you're using has ETH in Ganache.
* **Invalid Credentials**: Do not use an address string as a private key.

---

## 📒 References

* [Web3j Documentation](https://docs.web3j.io/)
* [Ganache by Truffle Suite](https://trufflesuite.com/ganache/)
* [Solidity Language](https://soliditylang.org/)

---

## 📄 License

Apache-2.0
