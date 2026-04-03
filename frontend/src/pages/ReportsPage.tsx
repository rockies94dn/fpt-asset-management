import { useQuery } from '@tanstack/react-query'
import { useState } from 'react'
import { api } from '../api/client'
import { apiUrl } from '../config/runtime'

export function ReportsPage() {
  const rooms = useQuery({ queryKey: ['reports', 'meta'], queryFn: api.reportsMeta })
  const [roomId, setRoomId] = useState('')

  const query = roomId ? `?roomId=${roomId}` : ''

  return (
    <>
      <div className="page-header">
        <div className="page-title">
          <i className="bi bi-file-earmark-bar-graph"></i>
          Xuất báo cáo
        </div>
      </div>

      <div className="row g-3 justify-content-center">
        <div className="col-lg-7">
          <div className="card">
            <div className="card-header">
              <span className="card-title">
                <i className="bi bi-sliders"></i>
                Tùy chọn xuất
              </span>
            </div>
            <div className="card-body">
              <div className="mb-4">
                <label className="form-label">Lọc theo phòng (để trống = tất cả)</label>
                <select id="roomFilter" className="form-select" value={roomId} onChange={(event) => setRoomId(event.target.value)}>
                  <option value="">-- Tất cả phòng --</option>
                  {rooms.data?.map((room) => (
                    <option key={room.id} value={room.id}>
                      {room.code} - {room.name}
                    </option>
                  ))}
                </select>
              </div>

              <div className="row g-3">
                <div className="col-sm-6">
                  <a className="react-export-card text-reset" href={apiUrl(`/api/reports/export/excel${query}`)}>
                    <div className="react-export-icon text-success">
                      <i className="bi bi-file-earmark-excel"></i>
                    </div>
                    <div className="react-export-title">Xuất Excel</div>
                    <div className="react-export-subtitle">Danh sách .xlsx đầy đủ</div>
                    <div className="mt-3">
                      <span className="btn btn-success btn-sm w-100">
                        <i className="bi bi-download me-1"></i> Tải xuống Excel
                      </span>
                    </div>
                  </a>
                </div>
                <div className="col-sm-6">
                  <a className="react-export-card text-reset" href={apiUrl(`/api/reports/export/pdf${query}`)}>
                    <div className="react-export-icon text-danger">
                      <i className="bi bi-file-earmark-pdf"></i>
                    </div>
                    <div className="react-export-title">Xuất PDF</div>
                    <div className="react-export-subtitle">Báo cáo in ấn</div>
                    <div className="mt-3">
                      <span className="btn btn-danger btn-sm w-100">
                        <i className="bi bi-download me-1"></i> Tải xuống PDF
                      </span>
                    </div>
                  </a>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  )
}
