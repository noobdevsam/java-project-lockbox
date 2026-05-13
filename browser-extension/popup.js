document.getElementById('get').addEventListener('click', () => {
  const site = document.getElementById('site').value;
  const message = { action: 'GET_ENTRY', site: site };

  chrome.runtime.sendNativeMessage('com.lockbox.bridge', message, (response) => {
    if (chrome.runtime.lastError) {
      document.getElementById('result').innerText = "Error: " + chrome.runtime.lastError.message;
    } else if (response) {
      document.getElementById('result').innerText = JSON.stringify(response);
    }
  });
});
