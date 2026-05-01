/**
 * Extracts an error message from an Axios error response.
 * Falls back to generic messages.
 */
export function getErrorMessage(error) {
  if (error?.response?.data?.message) {
    return error.response.data.message;
  }
  if (error?.response?.data?.errors) {
    const errors = error.response.data.errors;
    if (Array.isArray(errors)) return errors.join('. ');
    if (typeof errors === 'object') return Object.values(errors).flat().join('. ');
  }
  if (error?.message) return error.message;
  return 'Something went wrong. Please try again.';
}
