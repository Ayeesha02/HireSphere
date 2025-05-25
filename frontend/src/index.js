import React from 'react';
import { createRoot } from 'react-dom/client'; // Import createRoot
import App from './App';
import './style.css'; 

const root = createRoot(document.getElementById('root')); // Create a root
root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);