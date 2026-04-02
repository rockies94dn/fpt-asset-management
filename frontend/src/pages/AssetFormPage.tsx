import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { api } from '../api/client'

export function AssetFormPage() {
  const { assetId } = useParams()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const meta = useQuery({ queryKey: ['assets', 'meta'], queryFn: api.assetMeta })
  const asset = useQuery({
    queryKey: ['asset', assetId],
    queryFn: () => api.asset(assetId!),
    enabled: Boolean(assetId),
  })

  const [form, setForm] = useState({
    name: '',
    qaCode: '',
    categoryId: '',
    roomId: '',
    status: 'AVAILABLE',
    brand: '',
    model: '',
    serialNumber: '',
    purchaseDate: '',
    purchasePrice: '',
    warrantyExpiry: '',
    description: '',
    autoGenerateCode: true,
  })

  const save = useMutation({
    mutationFn: async () => {
      const payload = {
        ...form,
        categoryId: Number(form.categoryId),
        roomId: form.roomId ? Number(form.roomId) : null,
        purchasePrice: form.purchasePrice ? Number(form.purchasePrice) : null,
        purchaseDate: form.purchaseDate || null,
        warrantyExpiry: form.warrantyExpiry || null,
      }
      return assetId ? api.updateAsset(assetId, payload) : api.createAsset(payload)
    },
    onSuccess: async (result) => {
      await queryClient.invalidateQueries({ queryKey: ['assets'] })
      navigate(`/assets/${result.id}`)
    },
  })

  useEffect(() => {
    if (!asset.data) return
    const current = asset.data.asset
    setForm({
      name: current.name,
      qaCode: current.qaCode,
      categoryId: String(current.category?.id ?? ''),
      roomId: String(current.room?.id ?? ''),
      status: current.status,
      brand: current.brand ?? '',
      model: current.model ?? '',
      serialNumber: current.serialNumber ?? '',
      purchaseDate: current.purchaseDate ?? '',
      purchasePrice: current.purchasePrice?.toString() ?? '',
      warrantyExpiry: current.warrantyExpiry ?? '',
      description: current.description ?? '',
      autoGenerateCode: false,
    })
  }, [asset.data])

  return (
    <>
      <div className="page-header">
        <div className="page-title">
          <i className={assetId ? 'bi bi-pencil-square' : 'bi bi-plus-circle'}></i>
          {assetId ? 'Chỉnh sửa thiết bị' : 'Thêm thiết bị mới'}
        </div>
        <button className="btn btn-outline-secondary btn-sm" onClick={() => navigate(-1)}>
          <i className="bi bi-arrow-left me-1"></i>
          Quay lại
        </button>
      </div>

      <div className="row g-3">
        <div className="col-lg-8">
          <div className="card">
            <div className="card-header">
              <span className="card-title">
                <i className="bi bi-info-circle"></i>
                Thông tin cơ bản
              </span>
            </div>
            <div className="card-body">
              <div className="row g-3">
                <div className="col-md-6">
                  <label className="form-label">Mã QA</label>
                  <div className="input-group">
                    <input
                      className="form-control"
                      value={form.qaCode}
                      onChange={(event) => setForm({ ...form, qaCode: event.target.value, autoGenerateCode: false })}
                      placeholder={form.autoGenerateCode ? 'Sẽ được tạo tự động' : 'VD: FPT-PC-001'}
                      disabled={!assetId && form.autoGenerateCode}
                    />
                    {!assetId ? (
                      <span className="input-group-text bg-white">
                        <div className="form-check mb-0">
                          <input
                            className="form-check-input"
                            type="checkbox"
                            id="autoCode"
                            checked={form.autoGenerateCode}
                            onChange={(event) =>
                              setForm({
                                ...form,
                                autoGenerateCode: event.target.checked,
                                qaCode: event.target.checked ? '' : form.qaCode,
                              })
                            }
                          />
                          <label className="form-check-label" htmlFor="autoCode" style={{ fontSize: '12px', whiteSpace: 'nowrap' }}>
                            Tự động
                          </label>
                        </div>
                      </span>
                    ) : null}
                  </div>
                </div>

                <div className="col-md-6">
                  <label className="form-label">Tên thiết bị</label>
                  <input className="form-control" value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} />
                </div>

                <div className="col-md-6">
                  <label className="form-label">Loại thiết bị</label>
                  <select className="form-select" value={form.categoryId} onChange={(event) => setForm({ ...form, categoryId: event.target.value })}>
                    <option value="">-- Chọn loại --</option>
                    {meta.data?.categories.map((category) => (
                      <option key={category.id} value={category.id}>
                        {category.name}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="col-md-6">
                  <label className="form-label">Vị trí (phòng)</label>
                  <select className="form-select" value={form.roomId} onChange={(event) => setForm({ ...form, roomId: event.target.value })}>
                    <option value="">-- Chưa xác định --</option>
                    {meta.data?.rooms.map((room) => (
                      <option key={room.id} value={room.id}>
                        {room.code} - {room.name}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="col-md-6">
                  <label className="form-label">Trạng thái</label>
                  <select className="form-select" value={form.status} onChange={(event) => setForm({ ...form, status: event.target.value })}>
                    {meta.data?.statuses.map((status) => (
                      <option key={status.value} value={status.value}>
                        {status.label}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              <hr className="my-3" />
              <div className="card-title mb-3">
                <i className="bi bi-info-circle"></i> Thông tin chi tiết
              </div>

              <div className="row g-3">
                <div className="col-md-4">
                  <label className="form-label">Thương hiệu</label>
                  <input className="form-control" value={form.brand} onChange={(event) => setForm({ ...form, brand: event.target.value })} />
                </div>
                <div className="col-md-4">
                  <label className="form-label">Model</label>
                  <input className="form-control" value={form.model} onChange={(event) => setForm({ ...form, model: event.target.value })} />
                </div>
                <div className="col-md-4">
                  <label className="form-label">Số Serial</label>
                  <input className="form-control" value={form.serialNumber} onChange={(event) => setForm({ ...form, serialNumber: event.target.value })} />
                </div>
                <div className="col-md-4">
                  <label className="form-label">Ngày mua</label>
                  <input className="form-control" type="date" value={form.purchaseDate} onChange={(event) => setForm({ ...form, purchaseDate: event.target.value })} />
                </div>
                <div className="col-md-4">
                  <label className="form-label">Giá mua (VNĐ)</label>
                  <input className="form-control" type="number" value={form.purchasePrice} onChange={(event) => setForm({ ...form, purchasePrice: event.target.value })} />
                </div>
                <div className="col-md-4">
                  <label className="form-label">Hết bảo hành</label>
                  <input className="form-control" type="date" value={form.warrantyExpiry} onChange={(event) => setForm({ ...form, warrantyExpiry: event.target.value })} />
                </div>
                <div className="col-12">
                  <label className="form-label">Mô tả</label>
                  <textarea className="form-control" rows={3} value={form.description} onChange={(event) => setForm({ ...form, description: event.target.value })} />
                </div>
              </div>

              {save.error ? <div className="toast mt-3">{save.error.message}</div> : null}

              <div className="d-flex gap-2 mt-4">
                <button className="btn btn-primary px-4" onClick={() => save.mutate()} disabled={save.isPending || !form.name || !form.categoryId}>
                  <i className="bi bi-check-lg me-1"></i>
                  {save.isPending ? 'Đang lưu...' : assetId ? 'Lưu thay đổi' : 'Thêm thiết bị'}
                </button>
                <button className="btn btn-outline-secondary px-4" onClick={() => navigate(-1)}>
                  Hủy
                </button>
              </div>
            </div>
          </div>
        </div>

        <div className="col-lg-4">
          <div className="card mb-3">
            <div className="card-header">
              <span className="card-title">
                <i className="bi bi-lightbulb"></i>
                Hướng dẫn
              </span>
            </div>
            <div className="card-body" style={{ fontSize: '13px', color: '#6b7280', lineHeight: 1.8 }}>
              <div className="mb-2"><i className="bi bi-check-circle text-success me-2"></i><strong>Mã QA</strong> sẽ được in lên QR Code dán vào thiết bị</div>
              <div className="mb-2"><i className="bi bi-check-circle text-success me-2"></i>Chọn <strong>Tự động</strong> để hệ thống tạo mã duy nhất</div>
              <div className="mb-2"><i className="bi bi-check-circle text-success me-2"></i>Sau khi thêm, bạn có thể <strong>in QR Code</strong> từ trang chi tiết</div>
              <div><i className="bi bi-check-circle text-success me-2"></i>Gắn mã QR vào thiết bị để dễ dàng quét và kiểm tra</div>
            </div>
          </div>

          <div className="card">
            <div className="card-header">
              <span className="card-title">
                <i className="bi bi-lock"></i>
                Ghi chú
              </span>
            </div>
            <div className="card-body">
              <p style={{ fontSize: '13px', color: '#6b7280', marginBottom: 0 }}>
                Thay đổi phòng, trạng thái đặc biệt và nghiệp vụ phê duyệt vẫn được xử lý ở backend để giữ đúng quy tắc hệ thống.
              </p>
            </div>
          </div>
        </div>
      </div>
    </>
  )
}
