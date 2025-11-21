const chatMessages = document.getElementById('chatMessages');
const chatInput = document.getElementById('chatInput');
const sendButton = document.getElementById('sendButton');
const modelSelect = document.getElementById('modelSelect');

function addMessage(content, isUser = false) {
  const messageDiv = document.createElement('div');
  messageDiv.className = `message ${isUser ? 'user-message' : 'ai-message'}`;
  
  const contentDiv = document.createElement('div');
  contentDiv.className = 'message-content';
  contentDiv.innerHTML = isUser ? content : `<strong>${modelSelect.value.toUpperCase()}:</strong> ${content}`;
  
  messageDiv.appendChild(contentDiv);
  chatMessages.appendChild(messageDiv);
  chatMessages.scrollTop = chatMessages.scrollHeight;
}

function showApiKeyModal(service) {
  const modal = document.createElement('div');
  modal.className = 'api-key-modal';
  const existingKey = keyManager.getKey(service);
  modal.innerHTML = `
    <div class="modal-content">
      <h3>${existingKey ? 'Update' : 'Enter'} ${service.toUpperCase()} API Key</h3>
      <input type="password" id="apiKeyInput" placeholder="Enter your API key" value="${existingKey || ''}">
      <div class="modal-buttons">
        <button onclick="saveApiKey('${service}')">Save</button>
        <button onclick="closeModal()">Cancel</button>
      </div>
    </div>
  `;
  document.body.appendChild(modal);
}

function showManageKeysModal() {
  const modal = document.createElement('div');
  modal.className = 'api-key-modal';
  const hasOpenAI = keyManager.hasKey('openai');
  const hasGemini = keyManager.hasKey('gemini');
  
  modal.innerHTML = `
    <div class="modal-content">
      <h3>Manage API Keys</h3>
      <div class="key-list">
        <div class="key-item">
          <span>OpenAI (GPT): ${hasOpenAI ? '✅ Saved' : '❌ Not set'}</span>
          <button onclick="showApiKeyModal('openai')">${hasOpenAI ? 'Update' : 'Add'}</button>
        </div>
        <div class="key-item">
          <span>Gemini: ${hasGemini ? '✅ Saved' : '❌ Not set'}</span>
          <button onclick="showApiKeyModal('gemini')">${hasGemini ? 'Update' : 'Add'}</button>
        </div>
      </div>
      <div class="modal-buttons">
        <button onclick="clearAllKeys()">Clear All</button>
        <button onclick="closeModal()">Close</button>
      </div>
    </div>
  `;
  document.body.appendChild(modal);
}

function clearAllKeys() {
  if (confirm('Are you sure you want to clear all API keys?')) {
    keyManager.clearKeys();
    closeModal();
    addMessage('All API keys cleared.');
  }
}

function saveApiKey(service) {
  const input = document.getElementById('apiKeyInput');
  const key = input.value.trim();
  if (key) {
    keyManager.setKey(service, key);
    closeModal();
    addMessage(`${service.toUpperCase()} API key saved successfully!`);
  }
}

function closeModal() {
  const modal = document.querySelector('.api-key-modal');
  if (modal) modal.remove();
}

async function callOpenAI(message) {
  const key = keyManager.getKey('openai');
  if (!key) {
    showApiKeyModal('openai');
    throw new Error('OpenAI API key required');
  }
  
  const response = await fetch(API_CONFIG.ENDPOINTS.openai, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${key}`
    },
    body: JSON.stringify({
      model: API_CONFIG.MODELS.gpt.name,
      messages: [{ role: 'user', content: message }],
      max_tokens: API_CONFIG.MODELS.gpt.maxTokens
    })
  });
  
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.error?.message || `API Error: ${response.status}`);
  }
  
  const data = await response.json();
  return data.choices[0].message.content;
}

async function callGemini(message) {
  const key = keyManager.getKey('gemini');
  if (!key) {
    showApiKeyModal('gemini');
    throw new Error('Gemini API key required');
  }
  
  const modelName = API_CONFIG.MODELS.gemini.name;
  const url = `${API_CONFIG.ENDPOINTS.gemini}/models/${modelName}:generateContent?key=${key}`;
  
  console.log('Gemini URL:', url.replace(key, 'API_KEY_HIDDEN'));
  
  try {
    const response = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        contents: [{ parts: [{ text: message }] }]
      })
    });
    
    if (!response.ok) {
      const error = await response.json();
      console.error('Gemini API Error:', error);
      throw new Error(error.error?.message || `API Error: ${response.status}`);
    }
    
    const data = await response.json();
    return data.candidates[0].content.parts[0].text;
  } catch (error) {
    if (error.message === 'Failed to fetch') {
      throw new Error('Network error. Check your API key and internet connection.');
    }
    throw error;
  }
}

async function getAIResponse(userMessage, model) {
  try {
    let response;
    switch(model) {
      case 'gpt':
        response = await callOpenAI(userMessage);
        break;
      case 'gemini':
        response = await callGemini(userMessage);
        break;
      default:
        response = "Model not supported";
    }
    addMessage(response);
  } catch (error) {
    console.error('API Error:', error);
    addMessage(`Error: ${error.message || 'Failed to get response'}`);
  }
}

async function sendMessage() {
  const message = chatInput.value.trim();
  if (!message) return;
  
  addMessage(message, true);
  chatInput.value = '';
  
  addMessage('Thinking...');
  
  await getAIResponse(message, modelSelect.value);
  
  const messages = chatMessages.children;
  if (messages[messages.length - 1].textContent.includes('Thinking...')) {
    chatMessages.removeChild(messages[messages.length - 1]);
  }
}

sendButton.addEventListener('click', sendMessage);
chatInput.addEventListener('keypress', (e) => {
  if (e.key === 'Enter') sendMessage();
});

// Expose functions globally for onclick handlers
window.showManageKeysModal = showManageKeysModal;
window.showApiKeyModal = showApiKeyModal;
window.saveApiKey = saveApiKey;
window.closeModal = closeModal;
window.clearAllKeys = clearAllKeys;