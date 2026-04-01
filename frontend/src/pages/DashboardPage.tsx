import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { api } from '../api/client'
import { formatDateTime } from '../components/format'

export function DashboardPage() {
  const dashboard = useQuery({ queryKey: ['dashboard'], queryFn: api.dashboard })

  if (dashboard.isLoading) {
    return <div className="toast">Loading dashboard...</div>
  }

  const data = dashboard.data
  if (!data) return null

  return (
    <>
      <div className="page-header">
        <div className="page-title">
          <i className="bi bi-speedometer2"></i>
          Tổng quan hệ thống
        </div>
        <div className="d-flex gap-2">
          <Link className="btn btn-primary btn-sm" to="/assets/new">
            <i className="bi bi-plus-circle me-1"></i>
            Thêm thiết bị
          </Link>
          <Link className="btn btn-outline-primary btn-sm" to="/usages?scanner=1">
            <i className="bi bi-qr-code-scan me-1"></i>
            Check-in QR
          </Link>
        </div>
      </div>

      <div className="row g-3 mb-4">
        <div className="col-6 col-lg-3">
          <div className="stat-card orange">
            <div className="stat-icon orange">
              <i className="bi bi-collection"></i>
            </div>
            <div className="stat-value">{data.summary.totalAssets}</div>
            <div className="stat-label">Tổng thiết bị</div>
          </div>
        </div>
        <div className="col-6 col-lg-3">
          <div className="stat-card green">
            <div className="stat-icon green">
              <i className="bi bi-check-circle"></i>
            </div>
            <div className="stat-value">{data.summary.availableAssets}</div>
            <div className="stat-label">Sẵn sàng</div>
          </div>
        </div>
        <div className="col-6 col-lg-3">
          <div className="stat-card blue">
            <div className="stat-icon blue">
              <i className="bi bi-arrow-left-right"></i>
            </div>
            <div className="stat-value">{data.summary.activeUsages}</div>
            <div className="stat-label">Đang sử dụng</div>
          </div>
        </div>
        <div className="col-6 col-lg-3">
          <div className="stat-card red">
            <div className="stat-icon red">
              <i className="bi bi-tools"></i>
            </div>
            <div className="stat-value">{data.summary.openTickets}</div>
            <div className="stat-label">Ticket đang mở</div>
          </div>
        </div>
      </div>

      <div className="row g-3 mb-4">
        <div className="col-lg-4">
          <div className="card h-100">
            <div className="card-header">
              <span className="card-title">
                <i className="bi bi-lightning-charge"></i>
                Thao tác nhanh
              </span>
            </div>
            <div className="card-body d-grid gap-3">
              <Link className="quick-action-btn" to="/usages?scanner=1">
                <div className="qa-icon" style={{ background: 'rgba(255,107,0,0.1)', color: '#FF6B00' }}>
                  <i className="bi bi-qr-code-scan"></i>
                </div>
                <div>
                  <div style={{ fontWeight: 600, fontSize: '14px' }}>Check-in thiết bị</div>
                  <div style={{ fontSize: '12px', color: '#6b7280' }}>Quét QR để mượn hoặc ghi nhận di chuyển</div>
                </div>
                <i className="bi bi-chevron-right ms-auto" style={{ color: '#d1d5db' }}></i>
              </Link>

              <Link className="quick-action-btn" to="/tickets">
                <div className="qa-icon" style={{ background: 'rgba(239,68,68,0.1)', color: '#EF4444' }}>
                  <i className="bi bi-tools"></i>
                </div>
                <div>
                  <div style={{ fontWeight: 600, fontSize: '14px' }}>Báo hỏng thiết bị</div>
                  <div style={{ fontSize: '12px', color: '#6b7280' }}>Tạo ticket và trao đổi với kỹ thuật</div>
                </div>
                <i className="bi bi-chevron-right ms-auto" style={{ color: '#d1d5db' }}></i>
              </Link>

              <Link className="quick-action-btn" to="/assets">
                <div className="qa-icon" style={{ background: 'rgba(16,185,129,0.1)', color: '#10B981' }}>
                  <i className="bi bi-pc-display"></i>
                </div>
                <div>
                  <div style={{ fontWeight: 600, fontSize: '14px' }}>Quản lý thiết bị</div>
                  <div style={{ fontSize: '12px', color: '#6b7280' }}>Theo dõi trạng thái và vị trí thiết bị</div>
                </div>
                <i className="bi bi-chevron-right ms-auto" style={{ color: '#d1d5db' }}></i>
              </Link>

              <Link className="quick-action-btn" to="/reports">
                <div className="qa-icon" style={{ background: 'rgba(59,130,246,0.1)', color: '#3B82F6' }}>
                  <i className="bi bi-file-earmark-arrow-down"></i>
                </div>
                <div>
                  <div style={{ fontWeight: 600, fontSize: '14px' }}>Xuất báo cáo</div>
                  <div style={{ fontSize: '12px', color: '#6b7280' }}>Excel và PDF cho kiểm tra cuối vòng</div>
                </div>
                <i className="bi bi-chevron-right ms-auto" style={{ color: '#d1d5db' }}></i>
              </Link>
            </div>
          </div>
        </div>

        <div className="col-lg-4">
          <div className="card h-100">
            <div className="card-header">
              <span className="card-title">
                <i className="bi bi-bell"></i>
                Cần xử lý
              </span>
            </div>
            <div className="card-body d-grid gap-3">
              <div className="react-detail-item">
                <div style={{ fontWeight: 600, fontSize: '14px', color: '#92400E' }}>Đang mượn</div>
                <div style={{ fontSize: '24px', fontWeight: 700, color: '#FF6B00' }}>{data.summary.activeUsages}</div>
              </div>
              <div className="react-detail-item">
                <div style={{ fontWeight: 600, fontSize: '14px', color: '#991B1B' }}>Ticket quá hạn SLA</div>
                <div style={{ fontSize: '24px', fontWeight: 700, color: '#EF4444' }}>{data.summary.overdueTickets}</div>
              </div>
              <div className="react-detail-item">
                <div style={{ fontWeight: 600, fontSize: '14px', color: '#9A3412' }}>Đang bảo trì</div>
                <div style={{ fontSize: '24px', fontWeight: 700, color: '#F59E0B' }}>{data.summary.pendingMaintenance}</div>
              </div>
              <div className="react-detail-item">
                <div style={{ fontWeight: 600, fontSize: '14px', color: '#334155' }}>Thiết bị thất lạc</div>
                <div style={{ fontSize: '24px', fontWeight: 700, color: '#475569' }}>{data.summary.lostAssets}</div>
              </div>
            </div>
          </div>
        </div>

        <div className="col-lg-4">
          <div className="card h-100">
            <div className="card-header">
              <span className="card-title">
                <i className="bi bi-chat-dots"></i>
                Ticket của tôi
              </span>
              <Link className="btn btn-sm btn-outline-primary" to="/tickets">
                Xem tất cả
              </Link>
            </div>
            <div className="card-body">
              {data.myTickets.length === 0 ? (
                <div className="empty-state py-4">
                  <div className="empty-state-icon">
                    <i className="bi bi-inbox"></i>
                  </div>
                  <div className="empty-state-text">Chưa có ticket nào liên quan tới bạn</div>
                </div>
              ) : (
                <div className="d-grid gap-3">
                  {data.myTickets.slice(0, 4).map((ticket) => (
                    <Link className="react-detail-item text-reset" to={`/tickets/${ticket.id}`} key={ticket.id}>
                      <div className="d-flex justify-content-between gap-2 align-items-start">
                        <div>
                          <div style={{ fontWeight: 700 }}>{ticket.ticketCode}</div>
                          <div style={{ fontSize: '13px', color: '#6b7280' }}>{ticket.asset.name}</div>
                        </div>
                        <span className={`badge-status badge-${ticket.status.toLowerCase()}`}>
                          {ticket.statusLabel}
                        </span>
                      </div>
                      <div style={{ fontSize: '12px', color: '#9ca3af', marginTop: '6px' }}>
                        Cập nhật {formatDateTime(ticket.lastActivityAt ?? ticket.reportedAt)}
                      </div>
                    </Link>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>
      </div>

      <div className="card mb-4">
        <div className="card-header">
          <span className="card-title">
            <i className="bi bi-exclamation-diamond"></i>
            Thiết bị cần chú ý
          </span>
          <Link className="btn btn-sm btn-outline-primary" to="/assets">
            Mở danh sách
          </Link>
        </div>
        <div className="card-body p-0">
          <div className="table-responsive">
            <table className="table table-hover mb-0">
              <thead>
                <tr>
                  <th>Mã QA</th>
                  <th>Tên thiết bị</th>
                  <th>Phòng</th>
                  <th>Trạng thái</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {data.attentionAssets.length === 0 ? (
                  <tr>
                    <td colSpan={5}>
                      <div className="empty-state py-4">
                        <div className="empty-state-icon">
                          <i className="bi bi-shield-check"></i>
                        </div>
                        <div className="empty-state-text">Hiện chưa có thiết bị nào cần cảnh báo</div>
                      </div>
                    </td>
                  </tr>
                ) : (
                  data.attentionAssets.map((asset) => (
                    <tr key={asset.id}>
                      <td>
                        <code style={{ background: '#FFF3E0', color: '#FF6B00', padding: '3px 8px', borderRadius: '5px', fontSize: '12px' }}>
                          {asset.qaCode}
                        </code>
                      </td>
                      <td>
                        <div style={{ fontWeight: 600 }}>{asset.name}</div>
                        <div style={{ fontSize: '12px', color: '#6b7280' }}>{asset.category?.name ?? 'Chưa phân loại'}</div>
                      </td>
                      <td style={{ fontSize: '13px', color: '#6b7280' }}>{asset.room?.name ?? '-'}</td>
                      <td>
                        <span className={`badge-status badge-${asset.status.toLowerCase()}`}>
                          {asset.statusLabel}
                        </span>
                      </td>
                      <td>
                        <Link className="btn btn-sm btn-outline-primary py-1 px-2" to={`/assets/${asset.id}`}>
                          <i className="bi bi-eye"></i>
                        </Link>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <div className="card">
        <div className="card-header">
          <span className="card-title">
            <i className="bi bi-clock-history"></i>
            Thiết bị gần đây
          </span>
          <Link className="btn btn-sm btn-outline-primary" to="/assets">
            Xem tất cả
          </Link>
        </div>
        <div className="card-body p-0">
          <div className="table-responsive">
            <table className="table table-hover mb-0">
              <thead>
                <tr>
                  <th>Mã QA</th>
                  <th>Tên thiết bị</th>
                  <th>Loại</th>
                  <th>Phòng</th>
                  <th>Trạng thái</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {data.recentAssets.length === 0 ? (
                  <tr>
                    <td colSpan={6}>
                      <div className="empty-state py-4">
                        <div className="empty-state-icon">
                          <i className="bi bi-inbox"></i>
                        </div>
                        <div className="empty-state-text">Chưa có thiết bị nào</div>
                      </div>
                    </td>
                  </tr>
                ) : (
                  data.recentAssets.map((asset) => (
                    <tr key={asset.id}>
                      <td>
                        <code style={{ background: '#FFF3E0', color: '#FF6B00', padding: '3px 8px', borderRadius: '5px', fontSize: '12px' }}>
                          {asset.qaCode}
                        </code>
                      </td>
                      <td style={{ fontWeight: 500 }}>{asset.name}</td>
                      <td style={{ fontSize: '13px' }}>{asset.category?.name ?? '-'}</td>
                      <td style={{ fontSize: '13px', color: '#6b7280' }}>{asset.room?.name ?? '-'}</td>
                      <td>
                        <span className={`badge-status badge-${asset.status.toLowerCase()}`}>
                          {asset.statusLabel}
                        </span>
                      </td>
                      <td>
                        <Link className="btn btn-sm btn-outline-primary py-1 px-2" to={`/assets/${asset.id}`}>
                          <i className="bi bi-eye"></i>
                        </Link>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </>
  )
}
