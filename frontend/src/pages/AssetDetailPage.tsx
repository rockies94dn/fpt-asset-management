import { useQuery } from '@tanstack/react-query'
import { Link, useParams } from 'react-router-dom'
import { api } from '../api/client'
import { getAssetUsageDisabledReason } from '../components/assetUsage'
import { formatDate, formatMoney } from '../components/format'
import { statusClassName } from '../components/status'

export function AssetDetailPage() {
  const { assetId = '' } = useParams()
  const asset = useQuery({ queryKey: ['asset', assetId], queryFn: () => api.asset(assetId) })

  if (asset.isLoading) return <div className="toast">Loading asset...</div>
  if (!asset.data) return <div className="empty-state">Asset not found.</div>

  const current = asset.data.asset
  const maintenanceHistory = asset.data.maintenanceHistory
  const usageDisabledReason = getAssetUsageDisabledReason(current)
  const reportBrokenDisabledReason = current.status === 'BROKEN'
    ? 'Thiết bị đang ở trạng thái hỏng, không thể tạo thêm báo hỏng.'
    : null

  return (
    <>
      <div className="page-header">
        <div className="page-title">
          <i className={`bi ${current.category?.icon ?? 'bi-box'}`}></i>
          <span>{current.name}</span>
        </div>
        <div className="d-flex gap-2">
          <Link className="btn btn-outline-primary btn-sm" to={`/assets/${current.id}/edit`}>
            <i className="bi bi-pencil me-1"></i>
            Chỉnh sửa
          </Link>
          <Link className="btn btn-outline-secondary btn-sm" to="/assets">
            <i className="bi bi-arrow-left me-1"></i>
            Quay lại
          </Link>
        </div>
      </div>

      <div className="row g-3">
        <div className="col-lg-8">
          <div
            className="mb-3 p-3 rounded-3 d-flex align-items-center gap-3"
            style={{
              background:
                current.status === 'AVAILABLE'
                  ? 'rgba(16,185,129,0.08)'
                  : current.status === 'IN_USE'
                    ? 'rgba(59,130,246,0.08)'
                    : current.status === 'BROKEN'
                      ? 'rgba(239,68,68,0.08)'
                      : current.status === 'MAINTENANCE'
                        ? 'rgba(245,158,11,0.08)'
                        : 'rgba(100,116,139,0.08)',
              border:
                current.status === 'AVAILABLE'
                  ? '1px solid rgba(16,185,129,0.2)'
                  : current.status === 'IN_USE'
                    ? '1px solid rgba(59,130,246,0.2)'
                    : current.status === 'BROKEN'
                      ? '1px solid rgba(239,68,68,0.2)'
                      : current.status === 'MAINTENANCE'
                        ? '1px solid rgba(245,158,11,0.2)'
                        : '1px solid rgba(100,116,139,0.2)',
            }}
          >
            <span className={`badge-status badge-${statusClassName(current.status)}`} style={{ fontSize: '14px', padding: '8px 16px' }}>
              <i className="bi bi-circle-fill" style={{ fontSize: '8px' }}></i>
              <span>{current.statusLabel}</span>
            </span>
            <div>
              <div style={{ fontSize: '13px', fontWeight: 600 }}>Mã QA: {current.qaCode}</div>
              <div style={{ fontSize: '12px', color: '#6b7280' }}>Cập nhật gần nhất: {formatDate(current.updatedAt)}</div>
            </div>
          </div>

          <div className="card mb-3">
            <div className="card-header">
              <span className="card-title">
                <i className="bi bi-clipboard-data"></i>
                Thông tin thiết bị
              </span>
            </div>
            <div className="card-body">
              <div className="row g-3">
                <div className="col-sm-6">
                  <div className="react-info-item">
                    <span className="react-info-label">Tên thiết bị</span>
                    <span className="react-info-value">{current.name}</span>
                  </div>
                </div>
                <div className="col-sm-6">
                  <div className="react-info-item">
                    <span className="react-info-label">Loại thiết bị</span>
                    <span className="react-info-value">
                      {current.category ? (
                        <>
                          <i className={`bi ${current.category.icon} me-1`} style={{ color: '#FF6B00' }}></i>
                          {current.category.name}
                        </>
                      ) : 'N/A'}
                    </span>
                  </div>
                </div>
                <div className="col-sm-6">
                  <div className="react-info-item">
                    <span className="react-info-label">Vị trí (Phòng)</span>
                    <span className="react-info-value">
                      <i className="bi bi-geo-alt me-1" style={{ color: '#FF6B00' }}></i>
                      {current.room?.name ?? 'Chưa xác định'}
                    </span>
                  </div>
                </div>
                <div className="col-sm-6">
                  <div className="react-info-item">
                    <span className="react-info-label">Thương hiệu / Model</span>
                    <span className="react-info-value">{`${current.brand ?? ''} ${current.model ?? ''}`.trim() || '-'}</span>
                  </div>
                </div>
                <div className="col-sm-6">
                  <div className="react-info-item">
                    <span className="react-info-label">Số Serial</span>
                    <span className="react-info-value">{current.serialNumber ?? '-'}</span>
                  </div>
                </div>
                <div className="col-sm-6">
                  <div className="react-info-item">
                    <span className="react-info-label">Ngày mua</span>
                    <span className="react-info-value">{formatDate(current.purchaseDate)}</span>
                  </div>
                </div>
                <div className="col-sm-6">
                  <div className="react-info-item">
                    <span className="react-info-label">Giá mua</span>
                    <span className="react-info-value" style={{ color: '#FF6B00', fontWeight: 600 }}>
                      {formatMoney(current.purchasePrice)}
                    </span>
                  </div>
                </div>
                <div className="col-sm-6">
                  <div className="react-info-item">
                    <span className="react-info-label">Hết bảo hành</span>
                    <span className="react-info-value">{formatDate(current.warrantyExpiry)}</span>
                  </div>
                </div>
                {current.description ? (
                  <div className="col-12">
                    <div className="react-info-item">
                      <span className="react-info-label">Mô tả</span>
                      <span className="react-info-value">{current.description}</span>
                    </div>
                  </div>
                ) : null}
              </div>
            </div>
          </div>

          <div className="row g-2">
            <div className="col-sm-4">
              {usageDisabledReason ? (
                <button className="btn btn-secondary w-100" type="button" disabled title={usageDisabledReason}>
                  <i className="bi bi-slash-circle me-1"></i>
                  Không thể check-in
                </button>
              ) : (
                <Link className="btn btn-success w-100" to={`/usages?qaCode=${encodeURIComponent(current.qaCode)}`}>
                  <i className="bi bi-box-arrow-in-right me-1"></i>
                  Check-in
                </Link>
              )}
            </div>
            <div className="col-sm-4">
              {reportBrokenDisabledReason ? (
                <button className="btn btn-warning w-100" type="button" disabled title={reportBrokenDisabledReason}>
                  <i className="bi bi-tools me-1"></i>
                  Báo hỏng
                </button>
              ) : (
                <Link className="btn btn-warning w-100" to={`/tickets?qaCode=${encodeURIComponent(current.qaCode)}`}>
                  <i className="bi bi-tools me-1"></i>
                  Báo hỏng
                </Link>
              )}
            </div>
            <div className="col-sm-4">
              <Link className="btn btn-outline-primary w-100" to={`/assets/${current.id}/edit`}>
                <i className="bi bi-pencil me-1"></i>
                Chỉnh sửa
              </Link>
            </div>
          </div>

          <div className="card mt-3">
            <div className="card-header">
              <span className="card-title">
                <i className="bi bi-clock-history"></i>
                Lịch sử bảo trì theo mã QA
              </span>
            </div>
            <div className="card-body">
              {maintenanceHistory.length === 0 ? (
                <div className="empty-state py-4">
                  <div className="empty-state-icon">
                    <i className="bi bi-inbox"></i>
                  </div>
                  <div className="empty-state-text">Thiết bị này chưa có sự cố hoặc lần sửa chữa nào.</div>
                </div>
              ) : (
                <div className="react-asset-timeline">
                  {maintenanceHistory.map((item) => (
                    <div className="react-asset-timeline-item" key={item.id}>
                      <div className="react-asset-timeline-dot"></div>
                      <div className="react-asset-timeline-card">
                        <div className="d-flex flex-wrap justify-content-between align-items-start gap-2 mb-2">
                          <div>
                            <div style={{ fontWeight: 700, fontSize: '14px', color: '#1f2937' }}>{item.ticketCode}</div>
                            <div style={{ fontSize: '13px', color: '#6b7280' }}>{item.issueTypeLabel}</div>
                          </div>
                          <div className="d-flex flex-wrap gap-2">
                            <span className={`badge-status badge-${statusClassName(item.status)}`}>{item.statusLabel}</span>
                            {item.overdue ? <span className="badge-status badge-broken">Quá SLA</span> : null}
                          </div>
                        </div>

                        <div className="react-asset-timeline-meta">
                          <span><i className="bi bi-upc-scan me-1"></i>{current.qaCode}</span>
                          <span><i className="bi bi-geo-alt me-1"></i>{item.reportedRoomSnapshot ?? current.room?.name ?? 'Không xác định'}</span>
                          <span><i className="bi bi-person me-1"></i>{item.reportedBy.fullName}</span>
                          <span><i className="bi bi-wrench-adjustable-circle me-1"></i>{item.assignedTo?.fullName ?? 'Chưa phân công'}</span>
                        </div>

                        <div className="react-asset-timeline-dates">
                          <div>Báo lỗi: {formatDate(item.reportedAt)}</div>
                          <div>Cập nhật: {formatDate(item.lastActivityAt ?? item.reportedAt)}</div>
                          <div>Hoàn tất: {formatDate(item.resolvedAt)}</div>
                        </div>

                        {item.resolutionNote ? (
                          <div className="react-asset-timeline-note">
                            <div style={{ fontWeight: 600, marginBottom: '4px' }}>Ghi chú xử lý</div>
                            <div>{item.resolutionNote}</div>
                          </div>
                        ) : null}

                        <div className="mt-3">
                          <Link className="btn btn-sm btn-outline-primary" to={`/tickets/${item.id}`}>
                            <i className="bi bi-eye me-1"></i>
                            Xem ticket
                          </Link>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>

        <div className="col-lg-4">
          <div className="card text-center mb-3">
            <div className="card-header">
              <span className="card-title">
                <i className="bi bi-qr-code"></i>
                Mã QR thiết bị
              </span>
            </div>
            <div className="card-body">
              <div className="qr-container d-inline-block">
                <img src={`/assets/${current.id}/qr/download`} alt="QR Code" style={{ width: '200px', height: 'auto' }} />
                <div className="qr-label mt-2">{current.qaCode}</div>
              </div>
              <div className="d-flex gap-2 justify-content-center mt-3">
                <button className="btn btn-outline-primary btn-sm" type="button" onClick={() => window.open(`/assets/${current.id}/qr/download`, '_blank')}>
                  <i className="bi bi-printer me-1"></i>
                  In QR
                </button>
                <a className="btn btn-outline-secondary btn-sm" href={`/assets/${current.id}/qr/download`}>
                  <i className="bi bi-download me-1"></i>
                  Tải xuống
                </a>
              </div>
            </div>
          </div>

          <div className="card">
            <div className="card-body text-center">
              <div style={{ fontSize: '12px', color: '#6b7280', marginBottom: '8px' }}>Mã QA</div>
              <div style={{ background: '#FFF3E0', borderRadius: '8px', padding: '10px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                <code style={{ color: '#FF6B00', fontSize: '15px', fontWeight: 600 }}>{current.qaCode}</code>
                <button
                  className="btn btn-sm btn-link p-0 ms-2"
                  type="button"
                  onClick={() => {
                    void navigator.clipboard.writeText(current.qaCode)
                  }}
                >
                  <i className="bi bi-clipboard" style={{ color: '#FF6B00', fontSize: '16px' }}></i>
                </button>
              </div>
              <div style={{ fontSize: '11px', color: '#9ca3af', marginTop: '8px' }}>
                URL: /assets/scan/{current.qaCode}
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  )
}
