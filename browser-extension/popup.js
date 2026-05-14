let port = null;

function connect() {
  if (!port) {
    port = chrome.runtime.connectNative('com.lockbox.bridge');
    port.onMessage.addListener((response) => {
      const resDiv = document.getElementById('result');
      if (response.status === 'success') {
        resDiv.innerHTML = `<strong>User:</strong> ${response.username}<br><strong>Blob:</strong> <small style="color:gray">${response.password_blob.substring(0, 30)}...</small>`;
      } else {
        resDiv.innerText = "Error: " + response.message;
      }
    });
    port.onDisconnect.addListener(() => {
      port = null;
      document.getElementById('result').innerText = "Disconnected.";
    });
  }
}

document.getElementById('get').addEventListener('click', () => {
  const site = document.getElementById('site').value;
  if (!site) return;

  document.getElementById('result').innerText = "Fetching...";
  connect();
  port.postMessage({action: 'GET_ENTRY', site: site});
});

