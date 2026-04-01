import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from '../api/client'
import { formatDateTime } from '../components/format'

export function NotificationsPage() {
  const queryClient = useQueryClient()
  const notifications = useQuery({ queryKey: ['notifications'], queryFn: api.notifications })

  const readAll = useMutation({
    mutationFn: api.markNotificationsReadAll,
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['notifications'] })
      await queryClient.invalidateQueries({ queryKey: ['notifications', 'count'] })
    },
  })

  const markRead = useMutation({
    mutationFn: (id: number) => api.markNotificationRead(id),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['notifications'] })
      await queryClient.invalidateQueries({ queryKey: ['notifications', 'count'] })
    },
  })

  function normalizeNotificationHref(href: string) {
    if (href === '/maintenance') {
      return '/tickets'
    }
    if (href.startsWith('/maintenance/')) {
      return href.replace('/maintenance/', '/tickets/')
    }
    return href
  }

  return (
    <>
      <div className="page-header">
        <div className="page-title">
          <i className="bi bi-bell"></i>
          Thông báo
        </div>
        <button className="btn btn-primary btn-sm" onClick={() => readAll.mutate()} disabled={readAll.isPending}>
          Đánh dấu tất cả đã đọc
        </button>
      </div>

      <div className="card">
        <div className="card-header">
          <span className="card-title">
            <i className="bi bi-broadcast-pin"></i>
            Luồng cập nhật thời gian thực
          </span>
        </div>
        <div className="card-body p-0">
          {!notifications.data?.length ? (
            <div className="empty-state py-5">
              <div className="empty-state-icon">
                <i className="bi bi-bell-slash"></i>
              </div>
              <div className="empty-state-text">Chưa có thông báo nào</div>
            </div>
          ) : (
            <div className="notification-list" style={{ maxHeight: 'none' }}>
              {notifications.data.map((item) => (
                <div className={`notification-item ${item.read ? 'is-read' : 'is-unread'}`} key={item.id}>
                  <div className={`notification-icon tone-${item.tone ?? 'primary'}`}>
                    <i className={`bi ${item.icon ?? 'bi-bell'}`}></i>
                  </div>
                  <div className="notification-copy">
                    <div className="notification-title-row">
                      <div className="notification-title">{item.title}</div>
                      {item.count > 1 ? <span className="notification-count">{item.count}</span> : null}
                    </div>
                    <div className="notification-message">{item.message}</div>
                    <div className="notification-meta">{formatDateTime(item.updatedAt)}</div>
                    <div className="notification-actions">
                      {item.href ? (
                        <a className="notification-link-btn" href={`#${normalizeNotificationHref(item.href)}`}>
                          Mở liên kết
                        </a>
                      ) : null}
                      {!item.read ? (
                        <button className="btn btn-sm btn-outline-primary" onClick={() => markRead.mutate(item.id)}>
                          Đánh dấu đã đọc
                        </button>
                      ) : null}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </>
  )
}
