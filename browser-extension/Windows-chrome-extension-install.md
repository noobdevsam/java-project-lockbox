# Setting up LockBox Bridge on Windows

To set up the Native Messaging bridge on Windows, you must register the application in the Windows Registry.

### 1. Adjust the Bridge Script
The existing `run_lockbox_bridge.sh` is a shell script. On Windows, you will need a batch or PowerShell wrapper to execute the JAR.

**Create `run_lockbox_bridge.bat`**:
```batch
@echo off
java -jar "C:\path\to\your\LockBox.jar"
```

### 2. Update the Manifest
Modify `manifests/com.lockbox.bridge.json` to point to the batch file and use Windows-style paths (escaping backslashes):
```json
{
  "name": "com.lockbox.bridge",
  "description": "LockBox Native Messaging Host",
  "path": "C:\\path\\to\\run_lockbox_bridge.bat",
  "type": "stdio",
  "allowed_origins": ["chrome-extension://<YOUR_EXTENSION_ID_HERE>/"]
}
```

### 3. Register with Windows Registry
Chrome/Edge on Windows looks for native hosts in the Registry.

1.  Open `regedit`.
2.  Navigate to: `HKEY_CURRENT_USER\Software\Google\Chrome\NativeMessagingHosts\` 
    *(If using Edge, replace `Google\Chrome` with `Microsoft\Edge`)*.
3.  Right-click `NativeMessagingHosts` -> **New** -> **Key**. Name it `com.lockbox.bridge`.
4.  In the right pane, double-click the `(Default)` value and set its data to the **absolute path** of your `manifests/com.lockbox.bridge.json` file.

### 4. Verification
After restarting your browser, follow the same "How to Verify" steps from your `Readme.md`, replacing the bash execution with the batch file:
```batch
echo {"action":"GET_ENTRY", "site":"example.com"} | run_lockbox_bridge.bat
```

Ensure your `LockBox.jar` is accessible and your Java environment is in the system `PATH`.
