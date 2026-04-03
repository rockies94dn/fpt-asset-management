import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { api } from '../api/client'
import { getAssetUsageDisabledReason } from '../components/assetUsage'
import { InlineQrScanner } from '../components/InlineQrScanner'
import { formatDateTime } from '../components/format'
import { statusClassName } from '../components/status'
import { apiUrl } from '../config/runtime'

const usageStatusOptions = [
  { value: '', label: 'Tất cả trạng thái' },
  { value: 'ACTIVE', label: 'Đang sử dụng' },
  { value: 'COMPLETED', label: 'Đã hoàn trả' },
  { value: 'CANCELLED', label: 'Đã hủy' },
]

export function UsagePage() {
  const [searchParams] = useSearchParams()
  const queryClient = useQueryClient()
  const [keyword, setKeyword] = useState('')
  const [status, setStatus] = useState('')
  const [showForm, setShowForm] = useState(searchParams.get('scanner') === '1' || Boolean(searchParams.get('qaCode')))
  const [scannerOpen, setScannerOpen] = useState(searchParams.get('scanner') === '1')
  const [qaCode, setQaCode] = useState('')
  const [lookupCode, setLookupCode] = useState('')
  const [roomToId, setRoomToId] = useState('')
  const [purpose, setPurpose] = useState('')

  const usageFilters = new URLSearchParams()
  if (keyword.trim()) {
    usageFilters.set('keyword', keyword.trim())
  }
  if (status) {
    usageFilters.set('status', status)
  }

  const usages = useQuery({
    queryKey: ['usages', keyword, status],
    queryFn: () => api.usages(usageFilters),
  })
  const assetMeta = useQuery({ queryKey: ['assets', 'meta'], queryFn: api.assetMeta })
  const previewAsset = useQuery({
    queryKey: ['usage', 'preview', lookupCode],
    queryFn: () => api.scannedAsset(lookupCode),
    enabled: Boolean(lookupCode),
    retry: false,
  })
  const normalizedQaCode = qaCode.trim()
  const matchedPreviewAsset = previewAsset.data?.qaCode === normalizedQaCode ? previewAsset.data : null
  const usageDisabledReason = getAssetUsageDisabledReason(matchedPreviewAsset)
  const usageExportUrl = apiUrl(`/api/usages/export/excel${usageFilters.toString() ? `?${usageFilters.toString()}` : ''}`)

  useEffect(() => {
    if (!normalizedQaCode) {
      setLookupCode('')
      return
    }

    const timeoutId = window.setTimeout(() => {
      setLookupCode(normalizedQaCode)
    }, 250)

    return () => window.clearTimeout(timeoutId)
  }, [normalizedQaCode])

  useEffect(() => {
    const scannedCode = searchParams.get('qaCode') ?? ''
    if (scannedCode) {
      setShowForm(true)
      setQaCode(scannedCode)
      setLookupCode(scannedCode)
    }
    if (searchParams.get('scanner') === '1') {
      setShowForm(true)
      setScannerOpen(true)
    }
  }, [searchParams])

  const checkIn = useMutation({
    mutationFn: () => api.checkIn({ qaCode, roomToId: roomToId ? Number(roomToId) : null, purpose }),
    onSuccess: async () => {
      setShowForm(false)
      setQaCode('')
      setLookupCode('')
      setPurpose('')
      setRoomToId('')
      await queryClient.invalidateQueries({ queryKey: ['usages'] })
      await queryClient.invalidateQueries({ queryKey: ['dashboard'] })
    },
  })

  const checkOut = useMutation({
    mutationFn: (id: number) => api.checkOut(id, { note: 'Checked out from React console' }),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['usages'] })
      await queryClient.invalidateQueries({ queryKey: ['dashboard'] })
    },
  })

  return (
    <>
      <div className="page-header">
        <div className="page-title">
          <i className="bi bi-arrow-left-right"></i>
          Mượn / Trả
        </div>
        <button
          className="btn btn-primary"
          onClick={() => {
            if (showForm) {
              setShowForm(false)
              setScannerOpen(false)
              return
            }
            setShowForm(true)
            setScannerOpen(true)
          }}
        >
          <i className={`bi ${showForm ? 'bi-x-circle' : 'bi-qr-code-scan'} me-1`}></i>
          {showForm ? 'Đóng form Check-in' : 'Check-in thiết bị'}
        </button>
      </div>

      {showForm ? <div className="row g-3 justify-content-center mb-4">
        <div className="col-lg-6">
          <div className="card mb-3">
            <div className="card-header">
              <span className="card-title">
                <i className="bi bi-camera"></i>
                Quét mã QR
              </span>
              <button className="btn btn-sm btn-light border" onClick={() => setScannerOpen((value) => !value)}>
                <i className={`bi ${scannerOpen ? 'bi-stop-circle' : 'bi-camera'} me-1`}></i>
                {scannerOpen ? 'Dừng camera' : 'Mở camera'}
              </button>
            </div>
            <div className="card-body">
              <InlineQrScanner
                active={scannerOpen}
                onActiveChange={setScannerOpen}
                onDetected={(code) => {
                  setQaCode(code)
                  setLookupCode(code)
                }}
                showControls={false}
              />
            </div>
          </div>

          <div className="card">
            <div className="card-header">
              <span className="card-title">
                <i className="bi bi-keyboard"></i>
                Nhập thủ công
              </span>
            </div>
            <div className="card-body">
              <div className="mb-3">
                <label className="form-label">
                  Mã QA thiết bị <span className="text-danger">*</span>
                </label>
                <div className="input-group">
                  <span className="input-group-text bg-white">
                    <i className="bi bi-upc" style={{ color: '#FF6B00' }}></i>
                  </span>
                  <input
                    className="form-control"
                    placeholder="VD: FPT-PC-001"
                    value={qaCode}
                    onChange={(event) => setQaCode(event.target.value)}
                  />
                  <button type="button" className="btn btn-outline-primary" onClick={() => setLookupCode(normalizedQaCode)}>
                    <i className="bi bi-search"></i>
                  </button>
                </div>
              </div>

              {matchedPreviewAsset ? (
                <div className="mb-3 p-3 rounded-3" style={{ background: '#FFF3E0', border: '1px solid rgba(255,107,0,0.2)' }}>
                  <div className="d-flex align-items-center gap-3">
                    <div
                      style={{
                        width: '44px',
                        height: '44px',
                        background: 'white',
                        borderRadius: '10px',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        fontSize: '22px',
                        color: '#FF6B00',
                        boxShadow: '0 2px 8px rgba(0,0,0,0.08)',
                      }}
                    >
                        <i className={`bi ${matchedPreviewAsset.category?.icon ?? 'bi-box'}`}></i>
                      </div>
                    <div>
                      <div style={{ fontWeight: 600, fontSize: '14px' }}>{matchedPreviewAsset.name}</div>
                      <div style={{ fontSize: '12px', color: '#6b7280' }}>
                        <span>{matchedPreviewAsset.room?.name ?? 'Chưa có phòng'}</span>
                        {' · '}
                        <span className={`badge-status badge-${statusClassName(matchedPreviewAsset.status)}`} style={{ fontSize: '11px' }}>
                          {matchedPreviewAsset.statusLabel}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              ) : null}

              {usageDisabledReason ? (
                <div className="toast mb-3">
                  {usageDisabledReason} Vui lòng chọn thiết bị khác hoặc báo hỏng nếu cần.
                </div>
              ) : null}

              <div className="mb-3">
                <label className="form-label">Chuyển đến phòng</label>
                <select className="form-select" value={roomToId} onChange={(event) => setRoomToId(event.target.value)}>
                  <option value="">-- Giữ nguyên vị trí --</option>
                  {assetMeta.data?.rooms.map((room) => (
                    <option key={room.id} value={room.id}>
                      {room.code} - {room.name}
                    </option>
                  ))}
                </select>
                <div className="form-text">Kho hệ thống không thể được chọn thủ công.</div>
              </div>

              <div className="mb-4">
                <label className="form-label">Mục đích sử dụng</label>
                <textarea
                  className="form-control"
                  rows={2}
                  placeholder="Học lý thuyết, thực hành..."
                  value={purpose}
                  onChange={(event) => setPurpose(event.target.value)}
                />
              </div>

              {checkIn.error ? <div className="toast mb-3">{checkIn.error.message}</div> : null}

              <button
                className="btn btn-primary w-100 py-2"
                onClick={() => checkIn.mutate()}
                disabled={checkIn.isPending || !qaCode.trim() || Boolean(usageDisabledReason)}
                title={usageDisabledReason ?? undefined}
              >
                <i className="bi bi-box-arrow-in-right me-2"></i>
                {checkIn.isPending ? 'Đang xác nhận...' : 'Xác nhận Check-in'}
              </button>
            </div>
          </div>
        </div>

        <div className="col-lg-4">
          <div className="card">
            <div className="card-header">
              <span className="card-title">
                <i className="bi bi-info-circle"></i>
                Hướng dẫn
              </span>
            </div>
            <div className="card-body" style={{ fontSize: '13px', color: '#6b7280', lineHeight: 2 }}>
              <div>
                <span className="react-step-badge">1</span>
                Nhấn <strong>Mở camera</strong> để quét QR Code
              </div>
              <div>
                <span className="react-step-badge">2</span>
                Hoặc nhập <strong>Mã QA</strong> thủ công
              </div>
              <div>
                <span className="react-step-badge">3</span>
                Chọn phòng đích và mục đích sử dụng
              </div>
              <div>
                <span className="react-step-badge">4</span>
                Nhấn <strong>Xác nhận Check-in</strong>
              </div>

              <div className="mt-3 p-3 rounded-3" style={{ background: '#FEF2F2', border: '1px solid rgba(239,68,68,0.2)' }}>
                <div style={{ color: '#991B1B', fontWeight: 600, fontSize: '12px', marginBottom: '4px' }}>
                  <i className="bi bi-exclamation-triangle me-1"></i>
                  Lưu ý
                </div>
                <div style={{ color: '#991B1B', fontSize: '12px' }}>
                  Không thể check-in thiết bị đang bận, đang hỏng hoặc đang bảo trì.
                </div>
              </div>
            </div>
          </div>
        </div>
      </div> : null}

      {!showForm ? <div className="card mt-4">
        <div className="card-header">
          <span className="card-title">
            <i className="bi bi-clock-history"></i>
            Lịch sử mượn / trả
          </span>
        </div>
          <div className="card-body pb-0">
          <div className="row g-2 align-items-end mb-3">
            <div className="col-md-5">
              <label className="form-label mb-1">Tìm kiếm</label>
              <div className="input-group">
                <span className="input-group-text bg-white">
                  <i className="bi bi-search text-muted"></i>
                </span>
                <input
                  className="form-control"
                  placeholder="Tên thiết bị hoặc người dùng"
                  value={keyword}
                  onChange={(event) => setKeyword(event.target.value)}
                />
              </div>
            </div>
            <div className="col-md-3">
              <label className="form-label mb-1">Trạng thái</label>
              <select className="form-select" value={status} onChange={(event) => setStatus(event.target.value)}>
                {usageStatusOptions.map((option) => (
                  <option key={option.value || 'ALL'} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
            </div>
            <div className="col-md-4">
              <label className="form-label mb-1">Xuất dữ liệu</label>
              <a className="btn btn-success w-100" href={usageExportUrl}>
                <i className="bi bi-file-earmark-excel me-1"></i>
                Xuất Excel theo bộ lọc
              </a>
            </div>
          </div>
        </div>

        <div className="card-body p-0">
          <div className="table-responsive">
            <table className="table table-hover mb-0">
              <thead>
                <tr>
                  <th>Thiết bị</th>
                  <th>Người dùng</th>
                  <th>Check-in</th>
                  <th>Check-out</th>
                  <th>Trạng thái</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {usages.data?.content.length ? (
                  usages.data.content.map((usage) => (
                    <tr key={usage.id}>
                      <td>
                        <div style={{ fontWeight: 600 }}>{usage.asset.name}</div>
                        <div style={{ fontSize: '12px', color: '#6b7280' }}>{usage.asset.qaCode}</div>
                      </td>
                      <td>{usage.user.fullName}</td>
                      <td>{formatDateTime(usage.checkInTime)}</td>
                      <td>{usage.checkOutTime ? formatDateTime(usage.checkOutTime) : '-'}</td>
                      <td>
                        <span className={`badge-status ${usage.status === 'ACTIVE' ? 'badge-in_use' : 'badge-resolved'}`}>
                          {usage.status === 'ACTIVE' ? 'Đang sử dụng' : 'Hoàn tất'}
                        </span>
                      </td>
                      <td className="react-table-actions">
                        {usage.status === 'ACTIVE' ? (
                          <button className="btn btn-sm btn-outline-primary" onClick={() => checkOut.mutate(usage.id)}>
                            Check out
                          </button>
                        ) : null}
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan={6}>
                      <div className="empty-state py-4">
                        <div className="empty-state-icon">
                          <i className="bi bi-inbox"></i>
                        </div>
                        <div className="empty-state-text">Chưa có giao dịch nào</div>
                      </div>
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div> : null}
    </>
  )
}
