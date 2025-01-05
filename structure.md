project/
├── common/
│   ├── interfaces/
│   │   ├── GameServerInterface.java          // Interface for server communication
│   │   └── GameUpdateCallback.java           // Callback interface for real-time updates
│   ├── models/
│   │   ├── GameState.java                    // Shared game state model
│   │   ├── Move.java                         // Move model
│   │   └── Player.java                       // Player model (including IP for chat)
│   └── utils/
│       ├── Constants.java                    // Constants used across modules
│       └── Statistics.java                   // Shared statistics model
│
├── client/
│   ├── controllers/
│   │   ├── GameController.java              // Controls game logic
│   │   ├── GameManager.java                 // Manages game sessions
│   │   └── ChatController.java             // Handles P2P chat
│   ├── models/
│   │   ├── GameClient.java                 // Main client logic
│   │   ├── GameClientCallback.java         // Callback implementation
│   │   └── P2PChatClient.java             // Direct P2P chat implementation
│   ├── ui/
│   │   ├── ConsoleUI.java                  // Console-based UI
│   │   ├── GameGUI.java                    // Game GUI
│   │   └── ChatUI.java                     // Chat interface component
│   └── services/
│       └── GameService.java                // Handles communication with server
│
└── server/
    ├── controllers/
    │   ├── GameServer.java                 // Main server logic
    │   └── GameSession.java                // Handles game sessions
    ├── models/
    │   ├── GameServerInterface.java        // Interface for communication
    │   └── GameStatistics.java            // Server-side statistics
    └── services/
        ├── GameService.java                // Game logic service
        └── StatisticsService.java          // Statistics handling