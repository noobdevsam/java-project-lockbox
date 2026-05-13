  ## Instructions for Running the Application: 
  I am providing instructions on how to run and verify the newly secured Java application with the Native Messaging bridge.


✦ To run your application with the new Native Messaging bridge, follow these steps to ensure everything is connected and secure.

  1. Build and Prepare the Artifact
  First, compile your Java project into a runnable JAR (e.g., LockBox.jar). Ensure that all your project dependencies
  are either included in this JAR (a "fat JAR") or are available in the classpath when you run the app.

  2. Configure the Bridge
  Before running, you must point the bridge to your application:
   1. Edit run_lockbox_bridge.sh: Change /path/to/LockBox.jar to the actual absolute path of your compiled
      application JAR.
   2. Edit manifests/com.lockbox.bridge.json:
       * Change "path" to the absolute path where you placed the run_lockbox_bridge.sh script.
       * Update "allowed_origins" to match the Chrome Extension ID that you are using (e.g.,
         chrome-extension://abcdefghijklmno1234567890/).

  3. Register the Native Host
  You must register the bridge so the browser knows how to launch it:
   * Linux: Create the following directory if it doesn't exist: ~/.config/google-chrome/NativeMessagingHosts/
   * Action: Symlink or copy your manifests/com.lockbox.bridge.json file into that folder:
    ```
    cp manifests/com.lockbox.bridge.json ~/.config/google-chrome/NativeMessagingHosts/com.lockbox.bridge.json

  4. Running the Workflow
  To use the bridge, the application must be in an "unlocked" state:
   1. Launch the Desktop App: Open LockBox and log in (this initializes the AuthManager state to unlocked and sets
      the master key).
   2. Use the Browser: When your browser extension sends a request, the native host (launched automatically by
      Chrome) will check the AuthManager state.
       * If unlocked: Your MessageDispatcher will perform the lookup and return the data.
       * If locked: The bridge will return a JSON error message, protecting your data.
   3. Logout: When you log out of the LockBox desktop app, AuthManager clears the key and locks the bridge,
      immediately disabling further browser-based requests.

  How to Verify
  To test if the bridge is working without a full browser extension, you can manually simulate the browser's native
  messaging handshake from the terminal:

   1 # This sends a small JSON message to the script
   2 echo '{"action":"GET_ENTRY", "site":"example.com"}' | ./run_lockbox_bridge.sh
  Note: This command will fail with a "Vault is locked" message if the desktop application is not currently running
  and logged in.
