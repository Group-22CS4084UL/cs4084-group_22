# Expense Tracker App: Group22_CS4084
This Expense Tracker App is a group project for CS4004. Group Members are (in alphabetical order):
- Wenwen Deng, 23041374 
- Ruolin Li, 23007435
- Xiaoxuan Li, 23005491
- Yilin Wang, 23221356

The App follows a standard Android architecture with:
- Activities: UI controllers that handle user interactions
- SQLite Database: Local storage for transaction data
- Helper Classes: Utility classes for specific functionality
- XML Layouts: Define the UI structure and appearance
- Main Components

The features are categorised and listed as below:
1. Activities
- MainActivity: Central hub displaying financial summary and navigation options
- ExpenseActivity: Handles adding new expenses
- IncomeActivity: Handles adding new income
- TransactionListActivity: Displays transaction history with filtering options
- TransactionEditActivity: Allows editing existing transactions
2. Data Management
- DatabaseHelper: SQLite database manager handling all CRUD operations
- Transaction: Model class representing financial transactions
- TransactionAdapter: RecyclerView adapter for displaying transaction lists
3. Utility Classes
- NotificationHelper: Manages app notifications
- TutorialHelper: Handles first-time user tutorial
