import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useEffect, useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { api } from '../api/client'
import { InlineQrScanner } from '../components/InlineQrScanner'
import { formatDateTime } from '../components/format'

export function TicketListPage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const queryClient = useQueryClient()
  const [keyword, setKeyword] = useState('')
  const [showForm, setShowForm] = useState(Boolean(searchParams.get('qaCode')) || searchParams.get('scanner') === '1')
  const [scannerOpen, setScannerOpen] = useState(false)
  const [qaCode, setQaCode] = useState('')
  const [lookupCode, setLookupCode] = useState('')
  const [issueType, setIssueType] = useState('BROKEN')
  const [priority, setPriority] = useState('NORMAL')
  const [description, setDescription] = useState('')
  const [attachment, setAttachment] = useState<File | null>(null)

  const meta = useQuery({ queryKey: ['tickets', 'meta'], queryFn: api.ticketMeta })
  const previewAsset = useQuery({
    queryKey: ['ticket', 'preview', lookupCode],
    queryFn: () => api.scannedAsset(lookupCode),
    enabled: Boolean(lookupCode),
    retry: false,
  })
  const tickets = useQuery({
    queryKey: ['tickets', keyword],
    queryFn: () => api.tickets(new URLSearchParams(keyword ? { keyword } : {})),
  })

  const createTicket = useMutation({
    mutationFn: async () => {
      const formData = new FormData()
      formData.append('qaCode', qaCode)
      formData.append('issueType', issueType)
      formData.append('priority', priority)
      formData.append('description', description)
      if (attachment) formData.append('attachment', attachment)
      return api.createTicket(formData)
    },
    onSuccess: async (result) => {
      setQaCode('')
      setLookupCode('')
      setDescription('')
      setAttachment(null)
      setScannerOpen(false)
      await queryClient.invalidateQueries({ queryKey: ['tickets'] })
      await queryClient.invalidateQueries({ queryKey: ['dashboard'] })
      navigate(`/tickets/${result.ticket.id}`)
    },
  })

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

  return (
    <>
      <div className="page-header">
        <div className="page-title">
          <i className="bi bi-tools"></i>
          Báo hỏng / Bảo trì
        </div>
        <button className="btn btn-danger" onClick={() => setShowForm((value) => !value)}>
          <i className={`bi ${showForm ? 'bi-x-circle' : 'bi-exclamation-triangle'} me-1`}></i>
          {showForm ? 'Đóng form báo hỏng' : 'Báo hỏng'}
        </button>
      </div>

      {showForm ? <div className="row g-3 justify-content-center mb-4">
        <div className="col-lg-7">
          <div className="card">
            <div className="card-header">
              <span className="card-title">
                <i className="bi bi-file-earmark-text"></i>
                Phiếu báo hỏng
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
                    placeholder="Nhập hoặc quét mã QA"
                    value={qaCode}
                    onChange={(event) => setQaCode(event.target.value)}
                  />
                  <button type="button" className="btn btn-outline-secondary" onClick={() => setScannerOpen((value) => !value)}>
                    <i className="bi bi-qr-code-scan"></i>
                  </button>
                  <button type="button" className="btn btn-outline-primary" onClick={() => setLookupCode(qaCode.trim())}>
                    <i className="bi bi-search"></i>
                  </button>
                </div>

                {scannerOpen ? (
                  <div className="mt-2">
                    <InlineQrScanner
                      compact
                      active={scannerOpen}
                      onActiveChange={setScannerOpen}
                      onDetected={(code) => {
                        setQaCode(code)
                        setLookupCode(code)
                      }}
                      showControls={false}
                    />
                  </div>
                ) : null}
              </div>

              {previewAsset.data ? (
                <div className="mb-3 p-3 rounded-3" style={{ background: '#FFF3E0', border: '1px solid rgba(255,107,0,0.2)' }}>
                  <div style={{ fontWeight: 600 }}>{previewAsset.data.name}</div>
                  <div style={{ fontSize: '12px', color: '#6b7280' }}>
                    {previewAsset.data.room?.name ?? 'N/A'} {' · '} {previewAsset.data.category?.name ?? 'N/A'}
                  </div>
                </div>
              ) : null}

              <div className="row g-3">
                <div className="col-md-6">
                  <label className="form-label">
                    Loại yêu cầu <span className="text-danger">*</span>
                  </label>
                  <select className="form-select" value={issueType} onChange={(event) => setIssueType(event.target.value)}>
                    {meta.data?.issueTypes.map((type) => (
                      <option key={type.value} value={type.value}>
                        {type.label}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="col-md-6">
                  <label className="form-label">Mức độ ưu tiên</label>
                  <select className="form-select" value={priority} onChange={(event) => setPriority(event.target.value)}>
                    {meta.data?.priorities.map((level) => (
                      <option key={level.value} value={level.value}>
                        {level.label}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="col-12">
                  <label className="form-label">
                    Mô tả lỗi / yêu cầu <span className="text-danger">*</span>
                  </label>
                  <textarea
                    className="form-control"
                    rows={4}
                    placeholder="Mô tả chi tiết vấn đề, triệu chứng hỏng..."
                    value={description}
                    onChange={(event) => setDescription(event.target.value)}
                  />
                </div>
                <div className="col-12">
                  <label className="form-label">Ảnh hiện trường</label>
                  <input
                    className="form-control"
                    type="file"
                    accept="image/*"
                    capture="environment"
                    onChange={(event) => setAttachment(event.target.files?.[0] ?? null)}
                  />
                  <div className="form-text">Trên điện thoại, thao tác này sẽ mở camera sau để chụp ảnh sự cố.</div>
                </div>
              </div>

              {createTicket.error ? <div className="toast mt-3">{createTicket.error.message}</div> : null}

              <div className="d-flex gap-2 mt-4">
                <button
                  className="btn btn-danger px-4"
                  onClick={() => createTicket.mutate()}
                  disabled={createTicket.isPending || !qaCode.trim() || !description.trim()}
                >
                  <i className="bi bi-send me-1"></i>
                  {createTicket.isPending ? 'Đang gửi...' : 'Gửi báo cáo'}
                </button>
                <button
                  type="button"
                  className="btn btn-outline-secondary px-4"
                  onClick={() => {
                    setShowForm(false)
                    setQaCode('')
                    setLookupCode('')
                    setDescription('')
                    setAttachment(null)
                    setScannerOpen(false)
                  }}
                >
                  Hủy
                </button>
              </div>
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
                Nhập mã QA hoặc nhấn nút <strong>QR</strong> để bật camera
              </div>
              <div>
                <span className="react-step-badge">2</span>
                Kiểm tra đúng thiết bị ở khung xem trước
              </div>
              <div>
                <span className="react-step-badge">3</span>
                Chọn loại yêu cầu và mức ưu tiên phù hợp
              </div>
              <div>
                <span className="react-step-badge">4</span>
                Mô tả lỗi và gửi báo cáo cho kỹ thuật
              </div>

              <div className="mt-3 p-3 rounded-3" style={{ background: '#FFF3E0', border: '1px solid rgba(255,107,0,0.2)' }}>
                <div style={{ color: '#9A3412', fontWeight: 600, fontSize: '12px', marginBottom: '4px' }}>
                  <i className="bi bi-lightning-charge me-1"></i>
                  Gợi ý
                </div>
                <div style={{ color: '#9A3412', fontSize: '12px' }}>
                  Ưu tiên đính kèm ảnh hiện trường để kỹ thuật viên đánh giá nhanh hơn.
                </div>
              </div>
            </div>
          </div>
        </div>
      </div> : null}

      {!showForm ? <div className="card mt-4">
        <div className="card-header">
          <span className="card-title">
            <i className="bi bi-list-task"></i>
            Danh sách ticket
          </span>
          <span className="badge-status badge-pending">{tickets.data?.totalElements ?? 0} ticket</span>
        </div>
        <div className="card-body pb-0">
          <div className="row g-2 align-items-end mb-3">
            <div className="col-md-8">
              <label className="form-label mb-1">Tìm kiếm</label>
              <div className="input-group">
                <span className="input-group-text bg-white">
                  <i className="bi bi-search text-muted"></i>
                </span>
                <input
                  className="form-control"
                  placeholder="Mã ticket, thiết bị..."
                  value={keyword}
                  onChange={(event) => setKeyword(event.target.value)}
                />
              </div>
            </div>
          </div>
        </div>
        <div className="card-body p-0">
          <div className="table-responsive">
            <table className="table table-hover mb-0">
              <thead>
                <tr>
                  <th>Ticket</th>
                  <th>Thiết bị</th>
                  <th>Ưu tiên</th>
                  <th>Trạng thái</th>
                  <th>Cập nhật</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {tickets.data?.content.length ? (
                  tickets.data.content.map((ticket) => (
                    <tr key={ticket.id}>
                      <td>
                        <div style={{ fontWeight: 700 }}>{ticket.ticketCode}</div>
                        <div style={{ fontSize: '12px', color: '#6b7280' }}>{ticket.issueTypeLabel}</div>
                      </td>
                      <td>
                        <div style={{ fontWeight: 600 }}>{ticket.asset.name}</div>
                        <div style={{ fontSize: '12px', color: '#6b7280' }}>{ticket.asset.qaCode}</div>
                      </td>
                      <td>{ticket.priorityLabel}</td>
                      <td>
                        <div className="d-flex flex-wrap gap-2">
                          <span className={`badge-status badge-${ticket.status.toLowerCase()}`}>{ticket.statusLabel}</span>
                          {ticket.overdue ? <span className="badge-status badge-broken">Quá SLA</span> : null}
                        </div>
                      </td>
                      <td>{formatDateTime(ticket.lastActivityAt ?? ticket.reportedAt)}</td>
                      <td className="react-table-actions">
                        <Link className="btn btn-sm btn-outline-primary" to={`/tickets/${ticket.id}`}>
                          Chi tiết
                        </Link>
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
                        <div className="empty-state-text">Chưa có ticket nào</div>
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
