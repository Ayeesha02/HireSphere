import api from './api';

// Custom JWT decode function
const decodeToken = (token) => {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    const decoded = JSON.parse(jsonPayload);
    console.log('Decoded token:', decoded); // Debug log
    return decoded;
  } catch (e) {
    console.error('Failed to decode token:', e);
    return {};
  }
};

export const login = async (email, password, role) => {
  const endpoint = role === 'recruiter' ? '/recruiters/login' : '/candidates/login';
  const response = await api.post(endpoint, { email, password });
  const token = response.data.token;
  const decoded = decodeToken(token);
  const backendRole = decoded.role ? decoded.role.toLowerCase() : null;

  // If role isn’t in token, assume endpoint implies role (temporary fix)
  if (!backendRole) {
    console.warn('Role not found in token, assuming endpoint role');
    return token; // Skip role check for now
  }

  if (backendRole !== role.toLowerCase()) {
    throw new Error('Selected role does not match your account type.');
  }
  return token;
};

export const signUp = async (data, role) => {
  const endpoint = role === 'recruiter' ? '/recruiters/register/recruiter' : '/candidates/register';
  await api.post(endpoint, data);
};