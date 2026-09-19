package html4tree

/**
 * Exception thrown when a security policy file (e.g. .html4ignore) exists but cannot be read.
 * This enforces fail-closed behavior to mitigate TOCTOU vulnerabilities and policy bypasses.
 */
class IgnoreFileReadException(message: String) : Exception(message)
