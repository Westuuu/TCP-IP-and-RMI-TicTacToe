# TicTacToe Multiplayer Game

A Tic-Tac-Toe game implemented in Java using the RMI (Remote Method Invocation) framework for client-server communication and TCP/IP communication for P2P chat.

## Project Highlights

- **Client-Server Architecture**: Utilizes Java RMI for seamless remote communication
- **Peer-to-Peer Chat**: Integrated chat functionality allowing players to communicate during games
- **Game Rooms**: Support for multiple concurrent game sessions
- **Player Statistics**: Tracking of game statistics and player performance
- **Clean Architecture**: Modular design with clear separation of concerns

## Technical Details

- **Java**: Core language for both client and server implementations
- **RMI**: For distributed application communication
- **Multi-module Gradle Project**: Organized into Client, Server, and Common modules
- **Sockets**: Used for P2P chat functionality
- **Thread Management**: Proper concurrency handling for multiple game sessions

## Application Structure

- **Client**: UI components, game controllers, and networking for the player application
- **Server**: Game session management, matchmaking, and statistics tracking
- **Common**: Shared interfaces, models, and utilities used by both client and server

## Running the Application

1. Build the server and client:
   ```
   ./gradlew :Server:jar
   ./gradlew :Client:jar
   ```

2. Start the server:
   ```
   java -jar Server/build/libs/Server-1.0-SNAPSHOT.jar
   ```

3. Launch the client:
   ```
   java -jar Client/build/libs/Client-1.0-SNAPSHOT.jar
   ```
