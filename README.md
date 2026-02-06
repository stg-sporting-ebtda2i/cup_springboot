# cup_springboot

### Backend Setup
1. Clone the repository
1. Install the dependencies
    - Java 23
    - MySQL
    Check if you have Java and MySQL installed by running the following commands:
    ```bash
    java --version
    ```
    ```bash
    mysql --version
    ```
1. Copy .example.env to .env and add your credentials
    ```bash
    cp .env.example .env
    ```
1. Copy the private_key.pem in the same directory of .env
   ```bash
   cp /path/to/private_key.pem ./
   ```
1. Create a database in MySQL
    ```sql
    CREATE DATABASE cup;
    ```
1. Create the user for the database
    ```sql
    CREATE USER 'cup_user'@'%' IDENTIFIED BY 'password@123';
    ```
1. Grant the user all privileges on the database
    ```sql
   GRANT ALL PRIVILEGES ON cup.* TO 'cup_user'@'%';
    ```
1. Apply changes to the database
    ```sql
    FLUSH PRIVILEGES;
    ``` 
1. For maven 
    ```bash
    mvn clean install
    ```
    
### IMPORTANT NOTES
- Springboot will automatically create the tables in the database
- If you made a mistake in the entity, delete the table from the database and restart the springboot application
    ```
    DROP TABLE IF EXISTS <table_name>;
    ```
- To check schema of the database
    ```
    SHOW TABLES;
    ```
- To check the schema of a table
    ```
    DESCRIBE <table_name>;
    ```
