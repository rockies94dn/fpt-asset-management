function trimTrailingSlash(value: string): string {
  return value.replace(/\/+$/, '')
}

const rawApiBaseUrl = import.meta.env.VITE_API_BASE_URL?.trim() ?? ''
const rawWsBaseUrl = import.meta.env.VITE_WS_BASE_URL?.trim() ?? ''

export const apiBaseUrl = rawApiBaseUrl ? trimTrailingSlash(rawApiBaseUrl) : ''
export const webSocketBaseUrl = rawWsBaseUrl
  ? trimTrailingSlash(rawWsBaseUrl)
  : apiBaseUrl

export function apiUrl(path: string): string {
  if (/^https?:\/\//i.test(path)) {
    return path
  }

  const normalizedPath = path.startsWith('/') ? path : `/${path}`
  return apiBaseUrl ? `${apiBaseUrl}${normalizedPath}` : normalizedPath
}

export function webSocketUrl(path: string): string {
  const normalizedPath = path.startsWith('/') ? path : `/${path}`
  return webSocketBaseUrl ? `${webSocketBaseUrl}${normalizedPath}` : normalizedPath
}
