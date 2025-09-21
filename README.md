# kiahk_cup_springboot

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
1. Copy application.example.properties to application.properties
    ```bash
    cp .env.example .env
    ```
1. Create a database in MySQL
    ```sql
    CREATE DATABASE piehme_cup;
    ```
1. Create the user for the database
    ```sql
    CREATE USER 'piehme_cup_user'@'%' IDENTIFIED BY 'password@123';
    ```
1. Grant the user all privileges on the database
    ```sql
   GRANT ALL PRIVILEGES ON piehme_cup.* TO 'piehme_cup_user'@'%';
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
