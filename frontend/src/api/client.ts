import type {
  DashboardResponse,
  AssetDetail,
  MeResponse,
  NotificationItem,
  PageDto,
  Ticket,
  TicketDetail,
  Usage,
  Asset,
  CoverageRule,
  Room,
  User,
} from '../types/api'

type RequestOptions = RequestInit & {
  bodyJson?: unknown
}

const jsonHeaders: HeadersInit = {
  Accept: 'application/json',
}

let csrfToken: string | null = null

export function setCsrfToken(token: string | null) {
  csrfToken = token
}

async function ensureCsrfToken() {
  if (csrfToken) return csrfToken

  const response = await fetch('/api/me', {
    method: 'GET',
    credentials: 'include',
    headers: jsonHeaders,
  })

  if (!response.ok) {
    throw new Error('Unable to initialize session')
  }

  const contentType = response.headers.get('content-type') ?? ''
  if (!contentType.includes('application/json')) {
    throw new Error('Unable to initialize session')
  }

  const payload = (await response.json()) as { csrfToken?: string | null }
  csrfToken = payload.csrfToken ?? null

  if (!csrfToken) {
    throw new Error('Unable to initialize session')
  }

  return csrfToken
}

async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const headers = new Headers(options.headers ?? jsonHeaders)

  if (options.bodyJson !== undefined) {
    headers.set('Content-Type', 'application/json')
  }
  if (options.method && options.method !== 'GET') {
    const token = await ensureCsrfToken()
    headers.set('X-XSRF-TOKEN', token)
  } else if (csrfToken && options.method && options.method !== 'GET') {
    headers.set('X-XSRF-TOKEN', csrfToken)
  }

  const response = await fetch(path, {
    credentials: 'include',
    ...options,
    headers,
    body: options.bodyJson !== undefined ? JSON.stringify(options.bodyJson) : options.body,
  })

  if (!response.ok) {
    const fallback = 'Request failed'
    let message = fallback
    try {
      const errorJson = (await response.json()) as { message?: string }
      message = errorJson.message ?? fallback
    } catch {
      message = fallback
    }
    throw new Error(message)
  }

  if (response.status === 204) {
    return undefined as T
  }

  const contentType = response.headers.get('content-type') ?? ''
  if (!contentType.includes('application/json')) {
    return undefined as T
  }

  return (await response.json()) as T
}

export const api = {
  me: () => request<MeResponse>('/api/me'),
  login: (payload: { username: string; password: string }) =>
    request<MeResponse>('/api/auth/login', { method: 'POST', bodyJson: payload }),
  logout: () => request<{ message: string }>('/api/auth/logout', { method: 'POST' }),
  dashboard: () => request<DashboardResponse>('/api/dashboard'),
  assets: (params: URLSearchParams) => request<PageDto<Asset>>(`/api/assets?${params.toString()}`),
  asset: (id: string) => request<AssetDetail>(`/api/assets/${id}`),
  assetMeta: () =>
    request<{
      categories: { id: number; name: string; icon: string }[]
      rooms: Room[]
      statuses: { value: string; label: string }[]
    }>('/api/assets/meta'),
  createAsset: (payload: unknown) => request<Asset>('/api/assets', { method: 'POST', bodyJson: payload }),
  updateAsset: (id: string, payload: unknown) => request<Asset>(`/api/assets/${id}`, { method: 'PUT', bodyJson: payload }),
  usages: (params: URLSearchParams) => request<PageDto<Usage>>(`/api/usages?${params.toString()}`),
  checkIn: (payload: unknown) => request<Usage>('/api/usages/checkin', { method: 'POST', bodyJson: payload }),
  checkOut: (id: number, payload: unknown) => request<Usage>(`/api/usages/${id}/checkout`, { method: 'POST', bodyJson: payload }),
  tickets: (params: URLSearchParams) => request<PageDto<Ticket>>(`/api/tickets?${params.toString()}`),
  ticket: (id: string) => request<TicketDetail>(`/api/tickets/${id}`),
  ticketMeta: () =>
    request<{
      statuses: { value: string; label: string }[]
      priorities: { value: string; label: string }[]
      issueTypes: { value: string; label: string }[]
      technicians: User[]
    }>('/api/tickets/meta'),
  createTicket: async (formData: FormData) => {
    const token = await ensureCsrfToken()
    const response = await fetch('/api/tickets', {
      method: 'POST',
      credentials: 'include',
      headers: { 'X-XSRF-TOKEN': token },
      body: formData,
    })
    if (!response.ok) {
      const error = await response.json().catch(() => ({ message: 'Request failed' }))
      throw new Error(error.message ?? 'Request failed')
    }
    return response.json() as Promise<TicketDetail>
  },
  uploadTicketAttachment: async (ticketId: number, formData: FormData) => {
    const token = await ensureCsrfToken()
    const response = await fetch(`/api/tickets/${ticketId}/attachments`, {
      method: 'POST',
      credentials: 'include',
      headers: { 'X-XSRF-TOKEN': token },
      body: formData,
    })
    if (!response.ok) {
      const error = await response.json().catch(() => ({ message: 'Request failed' }))
      throw new Error(error.message ?? 'Request failed')
    }
    return response.json()
  },
  assignTicket: (id: number, assigneeId: number) =>
    request<Ticket>(`/api/tickets/${id}/assign`, { method: 'POST', bodyJson: { assigneeId } }),
  updateTicketStatus: (id: number, status: string) =>
    request<Ticket>(`/api/tickets/${id}/status`, { method: 'POST', bodyJson: { status } }),
  resolveTicket: (id: number, resolutionNote: string) =>
    request<Ticket>(`/api/tickets/${id}/resolve`, { method: 'POST', bodyJson: { resolutionNote } }),
  sendTicketMessage: (id: number, message: string) =>
    request(`/api/tickets/${id}/messages`, { method: 'POST', bodyJson: { message } }),
  notifications: () => request<NotificationItem[]>('/api/notifications'),
  notificationCount: () => request<{ unreadCount: number }>('/api/notifications/count'),
  markNotificationRead: (id: number) => request(`/api/notifications/${id}/read`, { method: 'POST' }),
  markNotificationsReadAll: () => request('/api/notifications/read-all', { method: 'POST' }),
  adminUsers: () => request<User[]>('/api/admin/users'),
  adminMeta: () =>
    request<{
      roles: { id: number; name: string }[]
      rooms: Room[]
      categories: { id: number; name: string; icon: string }[]
      technicians: User[]
    }>('/api/admin/meta'),
  toggleUser: (id: number) => request<User>(`/api/admin/users/${id}/toggle`, { method: 'POST' }),
  resetPassword: (id: number, newPassword: string) =>
    request(`/api/admin/users/${id}/reset-password`, { method: 'POST', bodyJson: { newPassword } }),
  adminRooms: () => request<Room[]>('/api/admin/rooms'),
  createRoom: (payload: unknown) => request<Room>('/api/admin/rooms', { method: 'POST', bodyJson: payload }),
  deleteRoom: (id: number) => request(`/api/admin/rooms/${id}`, { method: 'DELETE' }),
  coverageRules: () => request<CoverageRule[]>('/api/admin/coverage-rules'),
  createCoverageRule: (payload: unknown) =>
    request<CoverageRule>('/api/admin/coverage-rules', { method: 'POST', bodyJson: payload }),
  deleteCoverageRule: (id: number) => request(`/api/admin/coverage-rules/${id}`, { method: 'DELETE' }),
  reportsMeta: () => request<Room[]>('/api/reports/meta'),
  scannedAsset: (qaCode: string) => request<Asset>(`/api/assets/scan/${qaCode}`),
}
