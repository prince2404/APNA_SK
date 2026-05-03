/**
 * Extracts an error message from an Axios error response.
 * Handles network errors, timeouts, and API error responses.
 */
export function getErrorMessage(error) {
  // Cancelled requests
  if (error?.code === 'ERR_CANCELED') {
    return 'Request was cancelled';
  }

  // Network / timeout errors
  if (error?.code === 'ECONNABORTED' || error?.message?.includes('timeout')) {
    return 'Request timed out. Please check your connection and try again.';
  }

  if (error?.message === 'Network Error') {
    return 'Network error. Please check if the server is running.';
  }

  // API error response
  if (error?.response?.data?.message) {
    return error.response.data.message;
  }

  // Validation errors array
  if (error?.response?.data?.errors) {
    const errors = error.response.data.errors;
    if (Array.isArray(errors)) return errors.join('. ');
    if (typeof errors === 'object') return Object.values(errors).flat().join('. ');
  }

  // HTTP status fallbacks
  if (error?.response?.status) {
    const status = error.response.status;
    if (status === 401) return 'Session expired. Please log in again.';
    if (status === 403) return 'You do not have permission for this action.';
    if (status === 404) return 'Resource not found.';
    if (status === 500) return 'Server error. Please try again later.';
  }

  if (error?.message) return error.message;
  return 'Something went wrong. Please try again.';
}
