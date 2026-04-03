import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useMemo, useState } from 'react'
import { useParams } from 'react-router-dom'
import { api } from '../api/client'
import { formatDateTime } from '../components/format'
import { statusClassName } from '../components/status'
import { useRealtime } from '../hooks/useRealtime'
import { useSession } from '../hooks/useSession'

export function TicketDetailPage() {
  const { ticketId = '' } = useParams()
  const session = useSession()
  const queryClient = useQueryClient()
  const [message, setMessage] = useState('')
  const [attachment, setAttachment] = useState<File | null>(null)
  const ticket = useQuery({ queryKey: ['ticket', ticketId], queryFn: () => api.ticket(ticketId) })

  const subscriptions = useMemo(
    () => ({
      '/user/queue/tickets': (payload: unknown) => {
        const event = payload as { ticketId?: number }
        if (String(event.ticketId) !== ticketId) {
          return
        }
        void queryClient.invalidateQueries({ queryKey: ['ticket', ticketId] })
        void queryClient.invalidateQueries({ queryKey: ['tickets'] })
        void queryClient.invalidateQueries({ queryKey: ['dashboard'] })
      },
    }),
    [queryClient, ticketId],
  )
  useRealtime(Boolean(session.data?.authenticated), subscriptions)

  const sendMessage = useMutation({
    mutationFn: () => api.sendTicketMessage(Number(ticketId), message),
    onSuccess: async () => {
      setMessage('')
      await queryClient.invalidateQueries({ queryKey: ['ticket', ticketId] })
    },
  })

  const cancelTicket = useMutation({
    mutationFn: () => api.updateTicketStatus(Number(ticketId), 'CANCELLED'),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['ticket', ticketId] })
      await queryClient.invalidateQueries({ queryKey: ['tickets'] })
      await queryClient.invalidateQueries({ queryKey: ['dashboard'] })
    },
  })

  const resolve = useMutation({
    mutationFn: () => api.resolveTicket(Number(ticketId), 'Resolved from React workspace'),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['ticket', ticketId] })
      await queryClient.invalidateQueries({ queryKey: ['tickets'] })
      await queryClient.invalidateQueries({ queryKey: ['dashboard'] })
    },
  })

  const claimWork = useMutation({
    mutationFn: () => api.claimTicket(Number(ticketId)),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['ticket', ticketId] })
      await queryClient.invalidateQueries({ queryKey: ['tickets'] })
      await queryClient.invalidateQueries({ queryKey: ['dashboard'] })
    },
  })

  const startWork = useMutation({
    mutationFn: () => api.updateTicketStatus(Number(ticketId), 'IN_PROGRESS'),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['ticket', ticketId] })
      await queryClient.invalidateQueries({ queryKey: ['tickets'] })
      await queryClient.invalidateQueries({ queryKey: ['dashboard'] })
    },
  })

  const upload = useMutation({
    mutationFn: async () => {
      if (!attachment) return
      const formData = new FormData()
      formData.append('file', attachment)
      return api.uploadTicketAttachment(Number(ticketId), formData)
    },
    onSuccess: async () => {
      setAttachment(null)
      await queryClient.invalidateQueries({ queryKey: ['ticket', ticketId] })
    },
  })

  if (ticket.isLoading) return <div className="toast">Loading ticket...</div>
  if (ticket.isError) return <div className="toast">{ticket.error.message}</div>
  if (!ticket.data) return <div className="empty-state">Ticket not found.</div>

  const current = ticket.data.ticket
  const currentUser = session.data?.user
  const isAdmin = currentUser?.role === 'ADMIN'
  const isMaintenance = currentUser?.role === 'MAINTENANCE'
  const isAssignedTechnician = isMaintenance && current.assignedTo?.id === currentUser?.id
  const canClaimWork = ticket.data.claimable
  const canStartWork = current.status === 'PENDING' && isAssignedTechnician && !ticket.data.claimable
  const canResolve = current.status === 'IN_PROGRESS' && (isAssignedTechnician || isAdmin)
  const canCancel = isAdmin && (current.status === 'PENDING' || current.status === 'IN_PROGRESS')

  return (
    <>
      <div className="page-header">
        <div>
          <div className="page-title mb-2">
            <i className="bi bi-tools"></i>
            {current.asset.name}
          </div>
          <div className="d-flex flex-wrap gap-2">
            <span className="badge-status badge-pending">{current.ticketCode}</span>
            <span className={`badge-status badge-${statusClassName(current.status)}`}>{current.statusLabel}</span>
            {current.overdue ? <span className="badge-status badge-broken">Quá SLA</span> : null}
          </div>
        </div>
      </div>

      <div className="row g-3">
        <div className="col-lg-8">
          <div className="card h-100">
            <div className="card-header">
              <span className="card-title">
                <i className="bi bi-chat-dots"></i>
                Trao đổi theo ticket
              </span>
              <span style={{ fontSize: '12px', color: '#9ca3af' }}>/user/queue/tickets</span>
            </div>
            <div className="card-body">
              <div className="react-chat-thread mb-3">
                {ticket.data.messages.length === 0 ? (
                  <div className="empty-state py-4">
                    <div className="empty-state-icon">
                      <i className="bi bi-chat-square-text"></i>
                    </div>
                    <div className="empty-state-text">Chưa có trao đổi nào trong ticket này</div>
                  </div>
                ) : (
                  ticket.data.messages.map((item) => (
                    <div className={`react-ticket-message${item.sender.id === currentUser?.id ? ' self' : ''}`} key={item.id}>
                      <div style={{ fontWeight: 700 }}>{item.sender.fullName}</div>
                      <div>{item.message}</div>
                      <div className="chat-meta">{formatDateTime(item.createdAt)}</div>
                    </div>
                  ))
                )}
              </div>

              <div className="react-chat-composer">
                <textarea
                  className="form-control"
                  rows={4}
                  placeholder="Gửi cập nhật cho ticket"
                  value={message}
                  onChange={(event) => setMessage(event.target.value)}
                />
                <button className="btn btn-primary" onClick={() => sendMessage.mutate()} disabled={sendMessage.isPending || !message.trim()}>
                  Gửi tin nhắn
                </button>
              </div>
            </div>
          </div>
        </div>

        <div className="col-lg-4">
          <div className="card mb-3">
            <div className="card-header">
              <span className="card-title">
                <i className="bi bi-info-circle"></i>
                Thông tin ticket
              </span>
            </div>
            <div className="card-body d-grid gap-3">
              <div className="react-detail-item">
                <div style={{ fontWeight: 600 }}>Người báo</div>
                <div style={{ color: '#6b7280' }}>{current.reportedBy.fullName}</div>
              </div>
              <div className="react-detail-item">
                <div style={{ fontWeight: 600 }}>Kỹ thuật phụ trách</div>
                <div style={{ color: '#6b7280' }}>{current.assignedTo?.fullName ?? 'Chưa phân công'}</div>
              </div>
              <div className="react-detail-item">
                <div style={{ fontWeight: 600 }}>Candidate đang chờ nhận</div>
                <div style={{ color: '#6b7280' }}>
                  {ticket.data.candidateTechnicians.length
                    ? ticket.data.candidateTechnicians.map((technician) => technician.fullName).join(', ')
                    : 'Không còn candidate chờ nhận'}
                </div>
              </div>
              <div className="react-detail-item">
                <div style={{ fontWeight: 600 }}>SLA đến hạn</div>
                <div style={{ color: '#6b7280' }}>{formatDateTime(current.slaDueAt)}</div>
              </div>
              <div className="react-detail-item">
                <div style={{ fontWeight: 600 }}>Phòng báo lỗi</div>
                <div style={{ color: '#6b7280' }}>{current.reportedRoomSnapshot ?? current.asset.room?.name ?? 'Không xác định'}</div>
              </div>
            </div>
          </div>

          {isAdmin || isMaintenance ? (
            <div className="card mb-3">
              <div className="card-header">
                <span className="card-title">
                  <i className="bi bi-arrow-repeat"></i>
                  Cập nhật trạng thái
                </span>
              </div>
              <div className="card-body d-grid gap-3">
                {canClaimWork ? (
                  <button className="btn btn-primary" onClick={() => claimWork.mutate()} disabled={claimWork.isPending}>
                    Nhận việc
                  </button>
                ) : null}
                {canStartWork ? (
                  <button className="btn btn-primary" onClick={() => startWork.mutate()} disabled={startWork.isPending}>
                    Bắt đầu xử lý
                  </button>
                ) : null}
                {canResolve ? (
                  <button className="btn btn-outline-primary" onClick={() => resolve.mutate()} disabled={resolve.isPending}>
                    Đánh dấu đã xử lý
                  </button>
                ) : null}
                {canCancel ? (
                  <button className="btn btn-outline-secondary" onClick={() => cancelTicket.mutate()} disabled={cancelTicket.isPending}>
                    Hủy ticket
                  </button>
                ) : null}
                {!canClaimWork && !canStartWork && !canResolve && !canCancel ? (
                  <div className="text-muted" style={{ fontSize: '13px' }}>
                    {current.status === 'RESOLVED'
                      ? 'Ticket đã hoàn tất, không còn thao tác trạng thái.'
                      : current.status === 'CANCELLED'
                        ? 'Ticket đã bị hủy.'
                        : 'Chỉ candidate hợp lệ hoặc kỹ thuật viên đang sở hữu ticket mới có thể tiếp tục xử lý.'}
                  </div>
                ) : null}
              </div>
            </div>
          ) : null}

          <div className="card">
            <div className="card-header">
              <span className="card-title">
                <i className="bi bi-paperclip"></i>
                Tệp đính kèm
              </span>
            </div>
            <div className="card-body d-grid gap-3">
              <input
                className="form-control"
                type="file"
                accept="image/*"
                capture="environment"
                onChange={(event) => setAttachment(event.target.files?.[0] ?? null)}
              />
              <button className="btn btn-outline-primary" onClick={() => upload.mutate()} disabled={!attachment || upload.isPending}>
                Tải ảnh lên
              </button>
              <div className="d-grid gap-2">
                {ticket.data.attachments.length === 0 ? (
                  <div className="text-muted" style={{ fontSize: '13px' }}>Chưa có tệp nào</div>
                ) : (
                  ticket.data.attachments.map((item) => (
                    <a className="react-detail-item text-reset" href={item.downloadUrl} key={item.id} target="_blank" rel="noreferrer">
                      <div style={{ fontWeight: 600 }}>{item.originalName}</div>
                      <div style={{ fontSize: '12px', color: '#6b7280' }}>{formatDateTime(item.createdAt)}</div>
                    </a>
                  ))
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  )
}
