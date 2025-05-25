import React from 'react';

function ErrorMessage({ message }) {
  if (!message) return null;
  return <p style={{ color: 'red' }}>{message}</p>;
}

export default ErrorMessage;