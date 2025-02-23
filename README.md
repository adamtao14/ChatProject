

# Chat Client Application

A secure, real-time messaging client application built using Java RMI, featuring end-to-end encryption for secure communication.

## Features

- **User Authentication:** Secure registration and login with password hashing.
- **Contact Management:** Add, remove, and manage friends.
- **Encrypted Messaging:** Send and receive encrypted messages.
- **Search:** Find users and manage friend requests.

## System Architecture

- **Client-Server Model:** Uses Java RMI for communication.
- **Encryption:** Utilizes RSA for key exchange and AES for message encryption.
- **Database:** Stores user data, messages, and encryption keys securely.

## How It Works

1. **Register:** Users create an account and generate encryption keys.
2. **Add Friends:** Search for users and send friend requests.
3. **Message:** Communicate securely with end-to-end encryption.

## Setup

1. **Clone the Repository:**
   ```bash
   git clone https://github.com/your-username/progetto-di-algoritmi-distribuiti.git
   cd progetto-di-algoritmi-distribuiti
   ```

2. **Build and Run:**
   ```bash
   mvn clean install
   java -jar target/server.jar
   java -jar target/client.jar
   ```
