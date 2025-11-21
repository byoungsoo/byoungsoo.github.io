// Secure API Configuration - No keys stored in code
const API_CONFIG = {
  // API Endpoints
  ENDPOINTS: {
    openai: 'https://api.openai.com/v1/chat/completions',
    gemini: 'https://generativelanguage.googleapis.com/v1beta'
  },
  
  // Model configurations
  MODELS: {
    gpt: {
      name: 'gpt-5-nano',
      maxTokens: 150
    },
    gemini: {
      name: 'gemini-2.5-pro',
      maxTokens: 150
    }
  }
};

// Secure key management
class SecureKeyManager {
  constructor() {
    this.keys = {};
    this.loadKeysFromStorage();
  }
  
  loadKeysFromStorage() {
    try {
      const stored = localStorage.getItem('ai_api_keys');
      if (stored) {
        this.keys = JSON.parse(stored);
      }
    } catch (e) {
      console.warn('Could not load stored keys');
    }
  }
  
  saveKeysToStorage() {
    try {
      localStorage.setItem('ai_api_keys', JSON.stringify(this.keys));
    } catch (e) {
      console.warn('Could not save keys');
    }
  }
  
  setKey(service, key) {
    this.keys[service] = key;
    this.saveKeysToStorage();
  }
  
  getKey(service) {
    return this.keys[service] || null;
  }
  
  hasKey(service) {
    return !!this.keys[service];
  }
  
  clearKeys() {
    this.keys = {};
    localStorage.removeItem('ai_api_keys');
  }
}

const keyManager = new SecureKeyManager();