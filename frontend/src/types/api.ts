export type User = {
  id: number
  username: string
  fullName: string
  email: string | null
  phone: string | null
  role: string
  active: boolean
}

export type Room = {
  id: number
  code: string
  name: string
  building: string | null
  floor: number | null
  capacity: number | null
  active: boolean
}

export type Category = {
  id: number
  name: string
  icon: string
}

export type Asset = {
  id: number
  qaCode: string
  name: string
  status: string
  statusLabel: string
  category: Category | null
  room: Room | null
  brand: string | null
  model: string | null
  serialNumber: string | null
  purchaseDate: string | null
  purchasePrice: number | null
  warrantyExpiry: string | null
  description: string | null
  createdAt: string
  updatedAt: string
  available: boolean
  canBeUsed: boolean
}

export type AssetMaintenanceHistoryItem = {
  id: number
  ticketCode: string
  issueType: string
  issueTypeLabel: string
  priority: string
  priorityLabel: string
  status: string
  statusLabel: string
  reportedRoomSnapshot: string | null
  resolutionNote: string | null
  reportedAt: string
  resolvedAt: string | null
  lastActivityAt: string | null
  overdue: boolean
  reportedBy: User
  assignedTo: User | null
}

export type AssetDetail = {
  asset: Asset
  maintenanceHistory: AssetMaintenanceHistoryItem[]
}

export type TicketAttachment = {
  id: number
  originalName: string
  contentType: string
  size: number
  downloadUrl: string
  createdAt: string
  uploadedBy: User
}

export type ChatMessage = {
  id: number
  message: string
  messageType: string
  system: boolean
  createdAt: string
  sender: User
}

export type Ticket = {
  id: number
  ticketCode: string
  asset: Asset
  reportedBy: User
  assignedTo: User | null
  issueType: string
  issueTypeLabel: string
  priority: string
  priorityLabel: string
  status: string
  statusLabel: string
  reportedRoomSnapshot: string | null
  resolutionNote: string | null
  assignmentSource: string | null
  reportedAt: string
  resolvedAt: string | null
  slaDueAt: string | null
  slaBreachedAt: string | null
  lastActivityAt: string | null
  overdue: boolean
}

export type TicketDetail = {
  ticket: Ticket
  messages: ChatMessage[]
  attachments: TicketAttachment[]
}

export type Usage = {
  id: number
  asset: Asset
  user: User
  roomFrom: Room | null
  roomTo: Room | null
  checkInTime: string
  checkOutTime: string | null
  purpose: string | null
  note: string | null
  status: string
}

export type NotificationItem = {
  id: number
  key: string
  title: string
  message: string
  icon: string | null
  tone: string | null
  href: string | null
  count: number
  read: boolean
  createdAt: string
  updatedAt: string
}

export type CoverageRule = {
  id: number
  technician: User
  room: Room | null
  category: Category | null
  issueType: string | null
  sortOrder: number
  active: boolean
}

export type PageDto<T> = {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
}

export type DashboardResponse = {
  summary: {
    totalAssets: number
    availableAssets: number
    inUseAssets: number
    brokenAssets: number
    maintenanceAssets: number
    lostAssets: number
    activeUsages: number
    pendingMaintenance: number
    openTickets: number
    overdueTickets: number
  }
  recentAssets: Asset[]
  attentionAssets: Asset[]
  myTickets: Ticket[]
}

export type MeResponse = {
  authenticated: boolean
  csrfToken: string | null
  user: User | null
}
