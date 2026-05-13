How to use this extension:

   1. Open Extensions Page: In Chrome/Brave/Edge, navigate to chrome://extensions/.
   2. Enable Developer Mode: Toggle the Developer mode switch in the top-right corner.
   3. Load Unpacked: Click the Load unpacked button that appears.
   4. Select Directory: Select the /home/sam/MyProjects/JavaProjects/java-project-lockbox/browser-extension/ folder.
   5. Get ID: Once loaded, your extension will have an ID. Important: Copy this ID and update the allowed_origins
      field in your manifests/com.lockbox.bridge.json file to match this ID exactly (e.g.,
      "chrome-extension://<YOUR_ID_HERE>/").
   6. Refresh/Test: Click on the extension icon in your browser toolbar, enter a site name that exists in your
      database, and click Fetch Entry.

  Security Reminder
  Because you have integrated the AuthManager gatekeeper, this extension will only return data if your LockBox
  application is currently running and unlocked. If you haven't unlocked your vault, you will receive the "Vault is
  locked" error, confirming your security setup is working as expected.
