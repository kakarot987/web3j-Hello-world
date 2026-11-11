// SPDX-License-Identifier: Apache-2.0
pragma solidity ^0.8.19;

// Educational example. Deploy only to a disposable local development blockchain.
contract HelloWorld {
    address public immutable owner;
    string private currentGreeting;

    event GreetingChanged(string oldGreeting, string newGreeting);

    constructor(string memory initialGreeting) {
        owner = msg.sender;
        currentGreeting = initialGreeting;
    }

    modifier onlyOwner() {
        require(msg.sender == owner, "Only owner can change the greeting");
        _;
    }

    function greeting() external view returns (string memory) {
        return currentGreeting;
    }

    function newGreeting(string calldata newValue) external onlyOwner {
        emit GreetingChanged(currentGreeting, newValue);
        currentGreeting = newValue;
    }
}
