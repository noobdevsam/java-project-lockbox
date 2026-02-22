### **1\. Proposed Architectural Diagram**

This diagram shows the "Layered Architecture" of **LockBox**. It separates the user interface from the heavy-duty cryptographic logic and the database.

Mermaid Code:  
………………………….

\---  
config:  
  layout: elk  
\---  
flowchart LR  
 subgraph subGraph0\["Presentation Layer (Java Swing/FlatLaf)"\]  
        UI\["User Dashboard"\]  
        Login\["Login/Auth Screen"\]  
        Gen\["Password Generator"\]  
  end  
 subgraph subGraph1\["Business Logic Layer (Security Core)"\]  
        EM\["Encryption Manager"\]  
        KDF\["PBKDF2 Key Derivation"\]  
        AES\["AES-256-GCM Engine"\]  
  end  
 subgraph subGraph2\["Data Access Layer"\]  
        JDBC\["SQLite JDBC Driver"\]  
        DAO\["Data Access Object"\]  
  end  
 subgraph Storage\["Storage"\]  
        DB\[("Local SQLite File .db")\]  
  end  
    UI \--\> EM  
    Login \--\> KDF  
    EM \--\> AES  
    AES \--\> DAO  
    DAO \--\> JDBC  
    JDBC \--\> DB

### 

### ---

**2\. Logic Flowchart: Login & Vault Access**

This flowchart details the "Zero-Knowledge" authentication process. Notice how the system never stores your password; it only uses it to attempt to "unlock" the key needed for the database.

Mermaid Code:  
………………………..

\---  
config:  
  layout: elk  
\---  
flowchart LR  
 subgraph subGraph0\["Dashboard Actions"\]  
        Add\["Add New Entry"\]  
        Dashboard\["Open Vault Dashboard"\]  
        Encrypt\["AES-GCM Encrypt with Derived Key"\]  
        Save\["Save to SQLite"\]  
        View\["View Password"\]  
        Decrypt\["AES-GCM Decrypt with Derived Key"\]  
        Display\["Display Masked Text"\]  
  end  
    Start(\["Start Application"\]) \--\> CheckDB{"Database Exist?"}  
    CheckDB \-- No \--\> Register\["Create Master Password"\]  
    Register \--\> HashMP\["Hash Password \+ Salt"\]  
    HashMP \--\> SaveDB\[("Initialize SQLite DB")\]  
    SaveDB \--\> Dashboard  
    CheckDB \-- Yes \--\> Input\["Enter Master Password"\]  
    Input \--\> FetchSalt\["Retrieve Salt from DB"\]  
    FetchSalt \--\> Derive\["Derive Key via PBKDF2"\]  
    Derive \--\> Verify{"Verify Key?"}  
    Verify \-- Success \--\> Dashboard  
    Verify \-- Fail \--\> Error@{ label: "Show 'Invalid Password'" }  
    Error \--\> Input  
    Dashboard \--\> Add & View  
    Add \--\> Encrypt  
    Encrypt \--\> Save  
    View \--\> Decrypt  
    Decrypt \--\> Display

    Error@{ shape: rect}

### **Description of Components**

* **PBKDF2 Engine:** This is the "Speed Bump" for hackers. It takes a simple password and runs it through 600,000 iterations of hashing to create a 256-bit key. This makes it virtually impossible to brute-force locally.  
* **AES-GCM Engine:** This is the "Vault Door." It uses the key from the PBKDF2 engine to turn your passwords into unreadable blobs. The **GCM** mode also adds a "tag" that ensures no one has manually edited the database file.  
* **SQLite DAO:** The Data Access Object. This is the only part of your code that speaks "SQL." It handles the INSERT, SELECT, and UPDATE commands.

