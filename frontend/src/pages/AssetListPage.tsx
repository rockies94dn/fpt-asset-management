import { useQuery } from '@tanstack/react-query'
import { useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { api } from '../api/client'

export function AssetListPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const [keyword, setKeyword] = useState(searchParams.get('keyword') ?? '')
  const viewMode = searchParams.get('view') === 'grid' ? 'grid' : 'list'

  const meta = useQuery({ queryKey: ['assets', 'meta'], queryFn: api.assetMeta })
  const assets = useQuery({
    queryKey: ['assets', searchParams.toString()],
    queryFn: () => api.assets(searchParams),
  })

  useEffect(() => {
    setKeyword(searchParams.get('keyword') ?? '')
  }, [searchParams])

  function updateSearchParam(key: string, value: string) {
    const next = new URLSearchParams(searchParams)
    if (value) next.set(key, value)
    else next.delete(key)
    setSearchParams(next)
  }

  return (
    <>
      <div className="page-header">
        <div className="page-title">
          <i className="bi bi-pc-display"></i>
          Quản lý thiết bị
        </div>
        <Link className="btn btn-primary" to="/assets/new">
          <i className="bi bi-plus-circle me-1"></i>
          Thêm thiết bị
        </Link>
      </div>

      <div className="search-bar">
        <form
          onSubmit={(event) => {
            event.preventDefault()
            updateSearchParam('keyword', keyword.trim())
          }}
        >
          <div className="row g-2 align-items-end">
            <div className="col-md-8">
              <label className="form-label mb-1">Tìm kiếm</label>
              <div className="input-group">
                <span className="input-group-text bg-white">
                  <i className="bi bi-search text-muted"></i>
                </span>
                <input
                  className="form-control"
                  placeholder="Tên thiết bị, mã QA..."
                  value={keyword}
                  onChange={(event) => setKeyword(event.target.value)}
                />
              </div>
            </div>
            <div className="col-md-3">
              <label className="form-label mb-1">Trạng thái</label>
              <select
                className="form-select"
                value={searchParams.get('status') ?? ''}
                onChange={(event) => updateSearchParam('status', event.target.value)}
              >
                <option value="">-- Tất cả --</option>
                {meta.data?.statuses.map((status) => (
                  <option key={status.value} value={status.value}>
                    {status.label}
                  </option>
                ))}
              </select>
            </div>
            <div className="col-md-1">
              <button type="submit" className="btn btn-primary w-100">
                <i className="bi bi-search"></i>
              </button>
            </div>
          </div>
        </form>
      </div>

      <div className="d-flex justify-content-between align-items-center mb-3">
        <span style={{ fontSize: '13px', color: '#6b7280' }}>
          Tìm thấy <strong>{assets.data?.totalElements ?? 0}</strong> thiết bị
        </span>
        <div className="react-view-toggle">
          <button
            type="button"
            className={`react-view-toggle-btn${viewMode === 'grid' ? ' active' : ''}`}
            onClick={() => updateSearchParam('view', 'grid')}
            aria-label="Hiển thị dạng lưới"
          >
            <i className="bi bi-grid-3x3-gap-fill"></i>
          </button>
          <button
            type="button"
            className={`react-view-toggle-btn${viewMode === 'list' ? ' active' : ''}`}
            onClick={() => updateSearchParam('view', 'list')}
            aria-label="Hiển thị dạng danh sách"
          >
            <i className="bi bi-list-ul"></i>
          </button>
        </div>
      </div>

      <div className="table-wrapper">
        {assets.isLoading ? <div className="toast m-3">Loading assets...</div> : null}
        {!assets.isLoading && (assets.data?.content.length ?? 0) === 0 ? (
          <div className="empty-state">
            <div className="empty-state-icon">
              <i className="bi bi-pc-display"></i>
            </div>
            <div className="empty-state-text">Không tìm thấy thiết bị nào</div>
            <Link className="btn btn-primary mt-3" to="/assets/new">
              <i className="bi bi-plus-circle me-1"></i>
              Thêm thiết bị đầu tiên
            </Link>
          </div>
        ) : viewMode === 'grid' ? (
          <div className="react-asset-grid">
            {assets.data?.content.map((asset) => (
              <div className="react-asset-card" key={asset.id}>
                <div className="react-asset-card-header">
                  <div className="react-asset-icon">
                    <i className={`bi ${asset.category?.icon ?? 'bi-pc-display'}`}></i>
                  </div>
                  <span className={`badge-status badge-${asset.status.toLowerCase()}`}>
                    {asset.statusLabel}
                  </span>
                </div>

                <div className="react-asset-card-body">
                  <div className="react-asset-name">{asset.name}</div>
                  <div className="react-asset-meta">
                    <i className="bi bi-upc-scan"></i>
                    <span>{asset.qaCode}</span>
                  </div>
                  <div className="react-asset-meta">
                    <i className="bi bi-geo-alt"></i>
                    <span>{asset.room?.name ?? 'Chưa gán phòng'}</span>
                  </div>
                </div>

                <div className="react-asset-card-footer">
                  <div className="react-asset-category">{asset.category?.name ?? 'Chưa phân loại'}</div>
                  <div className="d-flex gap-1">
                    <Link className="btn btn-sm btn-outline-primary py-1 px-2" to={`/assets/${asset.id}`}>
                      <i className="bi bi-eye"></i>
                    </Link>
                    <Link className="btn btn-sm btn-outline-secondary py-1 px-2" to={`/assets/${asset.id}/edit`}>
                      <i className="bi bi-pencil"></i>
                    </Link>
                  </div>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="table-responsive">
            <table className="table table-hover mb-0">
              <thead>
                <tr>
                  <th>Mã QA</th>
                  <th>Tên thiết bị</th>
                  <th>Loại</th>
                  <th>Phòng</th>
                  <th>Thương hiệu</th>
                  <th>Trạng thái</th>
                  <th>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {assets.data?.content.map((asset) => (
                  <tr key={asset.id}>
                    <td>
                      <code style={{ background: '#FFF3E0', color: '#FF6B00', padding: '3px 8px', borderRadius: '5px', fontSize: '12px' }}>
                        {asset.qaCode}
                      </code>
                    </td>
                    <td style={{ fontWeight: 500 }}>{asset.name}</td>
                    <td style={{ fontSize: '13px' }}>{asset.category?.name ?? '-'}</td>
                    <td style={{ fontSize: '13px', color: '#6b7280' }}>{asset.room?.name ?? '-'}</td>
                    <td style={{ fontSize: '13px' }}>{asset.brand ?? '-'}</td>
                    <td>
                      <span className={`badge-status badge-${asset.status.toLowerCase()}`}>
                        {asset.statusLabel}
                      </span>
                    </td>
                    <td className="react-table-actions">
                      <div className="d-flex gap-1">
                        <Link className="btn btn-sm btn-outline-primary py-1 px-2" to={`/assets/${asset.id}`}>
                          <i className="bi bi-eye"></i>
                        </Link>
                        <Link className="btn btn-sm btn-outline-secondary py-1 px-2" to={`/assets/${asset.id}/edit`}>
                          <i className="bi bi-pencil"></i>
                        </Link>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </>
  )
}
