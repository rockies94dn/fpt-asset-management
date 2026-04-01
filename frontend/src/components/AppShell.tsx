import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useEffect, useRef, useState } from 'react'
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom'
import { api } from '../api/client'
import type { NotificationItem } from '../types/api'
import { formatDateTime } from './format'
import { useRealtime } from '../hooks/useRealtime'
import { useSession } from '../hooks/useSession'

const navItems = [
  { to: '/', label: 'Dashboard', icon: 'bi-speedometer2', adminOnly: false, section: 'Tổng quan' },
  { to: '/assets', label: 'Thiết bị', icon: 'bi-pc-display', adminOnly: false, section: 'Quản lý' },
  { to: '/usages', label: 'Mượn / Trả', icon: 'bi-arrow-left-right', adminOnly: false, section: 'Quản lý' },
  { to: '/tickets', label: 'Bảo trì / Báo hỏng', icon: 'bi-tools', adminOnly: false, section: 'Quản lý' },
  { to: '/reports', label: 'Xuất báo cáo', icon: 'bi-file-earmark-bar-graph', adminOnly: false, section: 'Báo cáo' },
  { to: '/admin', label: 'Quản trị', icon: 'bi-gear', adminOnly: true, section: 'Quản trị' },
]

const routeTitles = [
  ...navItems,
  { to: '/notifications', label: 'Thông báo', icon: 'bi-bell', adminOnly: false },
]

export function AppShell() {
  const location = useLocation()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const [notificationOpen, setNotificationOpen] = useState(false)
  const notificationRef = useRef<HTMLDivElement | null>(null)
  const session = useSession()
  const notificationCount = useQuery({
    queryKey: ['notifications', 'count'],
    queryFn: api.notificationCount,
    enabled: Boolean(session.data?.authenticated),
  })
  const notifications = useQuery({
    queryKey: ['notifications'],
    queryFn: api.notifications,
    enabled: Boolean(session.data?.authenticated),
  })
  const markRead = useMutation({
    mutationFn: (id: number) => api.markNotificationRead(id),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['notifications'] })
      await queryClient.invalidateQueries({ queryKey: ['notifications', 'count'] })
    },
  })
  const markReadAll = useMutation({
    mutationFn: api.markNotificationsReadAll,
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['notifications'] })
      await queryClient.invalidateQueries({ queryKey: ['notifications', 'count'] })
    },
  })

  useRealtime(Boolean(session.data?.authenticated), {
    '/user/queue/notifications': (payload) => {
      const notification = payload as NotificationItem
      queryClient.setQueryData<NotificationItem[] | undefined>(['notifications'], (current) => {
        const list = current ?? []
        const withoutExisting = list.filter((item) => item.id !== notification.id && item.key !== notification.key)
        return [notification, ...withoutExisting].slice(0, 10)
      })
      void queryClient.invalidateQueries({ queryKey: ['notifications'] })
      void queryClient.invalidateQueries({ queryKey: ['notifications', 'count'] })
    },
  })

  async function handleLogout() {
    await api.logout()
    await queryClient.invalidateQueries({ queryKey: ['session'] })
    navigate('/login')
  }

  function handleOpenScanner() {
    navigate(`/usages?scanner=1&ts=${Date.now()}`)
  }

  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (!notificationRef.current?.contains(event.target as Node)) {
        setNotificationOpen(false)
      }
    }

    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  useEffect(() => {
    setNotificationOpen(false)
  }, [location.pathname])

  const pageTitle = routeTitles.find((item) =>
    item.to === '/' ? location.pathname === '/' : location.pathname.startsWith(item.to),
  )?.label ?? 'Dashboard'

  const isAdmin = session.data?.user?.role === 'ADMIN'
  const topNotifications = notifications.data?.slice(0, 5) ?? []
  const visibleNavItems = navItems.filter((item) => !item.adminOnly || isAdmin)
  const roleLabel = session.data?.user?.role === 'ADMIN'
    ? 'Quản trị viên'
    : session.data?.user?.role === 'MAINTENANCE'
      ? 'Nhân viên bảo trì'
      : 'Nhân viên'

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
    <div className="react-shell">
      <div className={`sidebar${sidebarOpen ? ' show' : ''}`} id="sidebar">
        <div className="sidebar-brand">
          <div className="brand-logo">
            <i className="bi bi-cpu-fill"></i>
          </div>
          <div className="brand-text">
            <span className="brand-name">FPT Asset</span>
            <span className="brand-sub">Management System</span>
          </div>
        </div>

        <nav className="sidebar-nav">
          {visibleNavItems.map((item, index) => (
            <div key={item.to}>
              {index === 0 || visibleNavItems[index - 1].section !== item.section ? (
                <div className={`nav-section-label${index > 0 ? ' mt-3' : ''}`}>{item.section}</div>
              ) : null}
              <NavLink
                to={item.to}
                className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}
                end={item.to === '/'}
                onClick={() => setSidebarOpen(false)}
              >
                <i className={`bi ${item.icon}`}></i>
                <span>{item.label}</span>
              </NavLink>
            </div>
          ))}
        </nav>

        <div className="sidebar-footer">
          <div className="user-info">
            <div className="user-avatar">
              <i className="bi bi-person-fill"></i>
            </div>
            <div className="user-details">
              <span className="user-name">{session.data?.user?.fullName ?? session.data?.user?.username}</span>
              <span className="user-role">{roleLabel}</span>
            </div>
          </div>
          <button type="button" className="btn-logout border-0 bg-transparent" title="Đăng xuất" onClick={() => void handleLogout()}>
            <i className="bi bi-box-arrow-right"></i>
          </button>
        </div>
      </div>

      <div className="main-content">
        <nav className="top-navbar">
          <button type="button" className="sidebar-toggle" onClick={() => setSidebarOpen((value) => !value)}>
            <i className="bi bi-list"></i>
          </button>

          <div className="navbar-breadcrumb">
            <nav aria-label="breadcrumb">
              <ol className="breadcrumb">
                <li className="breadcrumb-item"><a href="#/">Dashboard</a></li>
                <li className="breadcrumb-item active">{pageTitle}</li>
              </ol>
            </nav>
          </div>

          <div className="navbar-actions">
            <button type="button" className="btn btn-sm btn-light border text-primary me-2 react-top-qr-btn" onClick={handleOpenScanner}>
              <i className="bi bi-qr-code-scan me-1"></i> Quét QR
            </button>
            <div className="react-notification-dropdown" ref={notificationRef}>
              <button
                type="button"
                className="notification-bell border-0"
                onClick={() => setNotificationOpen((value) => !value)}
                aria-expanded={notificationOpen}
              >
                <i className="bi bi-bell"></i>
                {(notificationCount.data?.unreadCount ?? 0) > 0 ? (
                  <span className="notification-badge">
                    {notificationCount.data!.unreadCount > 99 ? '99+' : notificationCount.data!.unreadCount}
                  </span>
                ) : null}
              </button>

              {notificationOpen ? (
                <div className="notification-menu dropdown-panel">
                  <div className="notification-menu-header">
                    <div className="notification-menu-title">Thông báo</div>
                    <div className="notification-menu-subtitle">
                      {(notificationCount.data?.unreadCount ?? 0) > 0
                        ? `${notificationCount.data?.unreadCount ?? 0} mục chưa đọc`
                        : 'Không có mục chưa đọc'}
                    </div>
                    {(notificationCount.data?.unreadCount ?? 0) > 0 ? (
                      <button
                        type="button"
                        className="notification-link-btn notification-action-btn"
                        onClick={() => markReadAll.mutate()}
                        disabled={markReadAll.isPending}
                      >
                        Đánh dấu tất cả đã đọc
                      </button>
                    ) : null}
                  </div>

                  <div className="notification-list">
                    {topNotifications.length === 0 ? (
                      <div className="empty-state py-4">
                        <div className="empty-state-icon">
                          <i className="bi bi-bell-slash"></i>
                        </div>
                        <div className="empty-state-text">Chưa có thông báo nào</div>
                      </div>
                    ) : (
                      topNotifications.map((item) => (
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
                            {item.href ? (
                              <div className="notification-actions">
                                <a
                                  className="notification-link-btn"
                                  href={`#${normalizeNotificationHref(item.href)}`}
                                  onClick={() => setNotificationOpen(false)}
                                >
                                  Mở chi tiết
                                </a>
                                {!item.read ? (
                                  <button
                                    type="button"
                                    className="notification-link-btn notification-action-btn"
                                    onClick={() => markRead.mutate(item.id)}
                                    disabled={markRead.isPending || markReadAll.isPending}
                                  >
                                    Đánh dấu đã đọc
                                  </button>
                                ) : null}
                              </div>
                            ) : null}
                          </div>
                        </div>
                      ))
                    )}
                  </div>

                  <div className="react-notification-footer">
                    <NavLink to="/notifications" className="notification-link-btn" onClick={() => setNotificationOpen(false)}>
                      Xem tất cả
                    </NavLink>
                  </div>
                </div>
              ) : null}
            </div>
          </div>
        </nav>

        <div className="page-content">
          <Outlet />
        </div>
      </div>
    </div>
  )
}
