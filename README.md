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
  ![屏幕截图 2025-03-27 121542](https://github.com/user-attachments/assets/307f6c3a-a30f-4cdc-9edb-fd4d7302b252)

- ExpenseActivity: Handles adding new expenses
- IncomeActivity: Handles adding new income
  ![屏幕截图 2025-03-27 121918](https://github.com/user-attachments/assets/f565dd01-223d-4811-af02-5b3caaa320aa)

- TransactionListActivity: Displays transaction history with filtering options
- TransactionEditActivity: Allows editing existing transactions
2. Data Management
- DatabaseHelper: SQLite database manager handling all CRUD operations
- Transaction: Model class representing financial transactions
- TransactionAdapter: RecyclerView adapter for displaying transaction lists
3. Utility Classes
- NotificationHelper: Manages app notifications
  ![屏幕截图 2025-03-27 122049](https://github.com/user-attachments/assets/ff9c1beb-43e4-42dd-9e8d-27d5bebc9cd8)

- TutorialHelper: Handles first-time user tutorial
  ![屏幕截图 2025-03-27 121256](https://github.com/user-attachments/assets/511ba2d6-763f-41c6-858b-6d798e11edf6)
4. UI/UX features
- Dark Mode

Testing environment: Elsa (Yilin Wang) cloned the repository and used the Medium Phone emulator to test our project locally. 

![image](https://github.com/user-attachments/assets/973aa60f-56d2-4b2c-8139-9c1347772580)

Project demo video:
![Video](https://github.com/user-attachments/assets/9990d0dd-7ef5-4198-9522-07a09c92131d)


