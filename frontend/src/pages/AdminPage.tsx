import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { api } from '../api/client'

export function AdminPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const queryClient = useQueryClient()
  const users = useQuery({ queryKey: ['admin', 'users'], queryFn: api.adminUsers })
  const rooms = useQuery({ queryKey: ['admin', 'rooms'], queryFn: api.adminRooms })

  const [roomForm, setRoomForm] = useState({ code: '', name: '', building: '', floor: '', capacity: '', description: '' })

  const createRoom = useMutation({
    mutationFn: () =>
      api.createRoom({
        ...roomForm,
        floor: roomForm.floor ? Number(roomForm.floor) : null,
        capacity: roomForm.capacity ? Number(roomForm.capacity) : null,
      }),
    onSuccess: async () => {
      setRoomForm({ code: '', name: '', building: '', floor: '', capacity: '', description: '' })
      await queryClient.invalidateQueries({ queryKey: ['admin', 'rooms'] })
      await queryClient.invalidateQueries({ queryKey: ['admin', 'meta'] })
    },
  })

  const deleteRoom = useMutation({
    mutationFn: (id: number) => api.deleteRoom(id),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['admin', 'rooms'] })
      await queryClient.invalidateQueries({ queryKey: ['admin', 'meta'] })
    },
  })

  const toggleUser = useMutation({
    mutationFn: (id: number) => api.toggleUser(id),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['admin', 'users'] })
    },
  })

  const activeRooms = rooms.data?.filter((room) => room.active) ?? []
  const currentTab = searchParams.get('tab') === 'rooms' ? 'rooms' : 'users'

  function setTab(tab: 'users' | 'rooms') {
    const next = new URLSearchParams(searchParams)
    next.set('tab', tab)
    setSearchParams(next, { replace: true })
  }

  return (
    <>
      <div className="page-header">
        <div className="page-title">
          <i className="bi bi-gear"></i>
          Quản trị hệ thống
        </div>
      </div>

      <div className="card mb-4">
        <div className="card-body py-3">
          <div className="d-flex flex-wrap gap-2">
            <button
              type="button"
              className={`btn ${currentTab === 'users' ? 'btn-primary' : 'btn-outline-primary'}`}
              onClick={() => setTab('users')}
            >
              <i className="bi bi-people me-1"></i>
              Quản lý người dùng
            </button>
            <button
              type="button"
              className={`btn ${currentTab === 'rooms' ? 'btn-primary' : 'btn-outline-primary'}`}
              onClick={() => setTab('rooms')}
            >
              <i className="bi bi-building me-1"></i>
              Quản lý phòng học
            </button>
          </div>
        </div>
      </div>

      {currentTab === 'users' ? (
        <div className="mb-4">
          <div className="page-header mb-3" style={{ paddingBottom: 0 }}>
            <div className="page-title" style={{ fontSize: '22px' }}>
              <i className="bi bi-people"></i>
              Quản lý người dùng
            </div>
          </div>
          <div className="table-wrapper">
            <div className="table-responsive">
              <table className="table table-hover mb-0">
                <thead>
                  <tr>
                    <th>#</th>
                    <th>Họ tên</th>
                    <th>Tài khoản</th>
                    <th>Email / SĐT</th>
                    <th>Quyền</th>
                    <th>Trạng thái</th>
                    <th>Thao tác</th>
                  </tr>
                </thead>
                <tbody>
                  {users.data?.map((user, index) => (
                    <tr key={user.id}>
                      <td style={{ color: '#9ca3af' }}>{index + 1}</td>
                      <td style={{ fontWeight: 600 }}>{user.fullName}</td>
                      <td>
                        <code style={{ background: '#f3f4f6', padding: '2px 8px', borderRadius: '5px', fontSize: '12px' }}>
                          {user.username}
                        </code>
                      </td>
                      <td style={{ fontSize: '13px', color: '#6b7280' }}>{user.email ?? user.phone ?? '-'}</td>
                      <td>
                        <span className={`react-role-pill ${user.role === 'ADMIN' ? 'danger' : user.role === 'MAINTENANCE' ? 'warning' : 'primary'}`}>
                          {user.role}
                        </span>
                      </td>
                      <td>
                        <span className={`badge-status ${user.active ? 'badge-available' : 'badge-broken'}`}>
                          {user.active ? 'Hoạt động' : 'Đã khóa'}
                        </span>
                      </td>
                      <td className="react-table-actions">
                        <button className={`btn btn-sm ${user.active ? 'btn-outline-warning' : 'btn-outline-success'}`} onClick={() => toggleUser.mutate(user.id)}>
                          <i className={`bi ${user.active ? 'bi-lock' : 'bi-unlock'} me-1`}></i>
                          {user.active ? 'Khóa' : 'Mở'}
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      ) : null}

      {currentTab === 'rooms' ? (
        <>
          <div className="page-header mb-3" style={{ paddingBottom: 0 }}>
            <div className="page-title" style={{ fontSize: '22px' }}>
              <i className="bi bi-building"></i>
              Quản lý phòng học
            </div>
          </div>

          <div className="row g-3 mb-4">
            <div className="col-lg-8">
              <div className="row g-3">
                {activeRooms.map((room) => (
                  <div className="col-sm-6 col-md-4" key={room.id}>
                    <div className="card h-100 react-room-card">
                      <div className="card-body">
                        <div className="d-flex align-items-center gap-3 mb-3">
                          <div className="react-room-icon">
                            <i className="bi bi-door-open"></i>
                          </div>
                          <div>
                            <div style={{ fontWeight: 700, fontSize: '15px' }}>{room.code}</div>
                            <div style={{ fontSize: '12px', color: '#6b7280' }}>{room.building ?? ''}</div>
                          </div>
                        </div>
                        <div style={{ fontWeight: 500, fontSize: '14px', marginBottom: '6px' }}>{room.name}</div>
                        <div style={{ fontSize: '12px', color: '#9ca3af' }}>
                          <i className="bi bi-people me-1"></i>
                          <span>{room.capacity != null ? `${room.capacity} chỗ` : 'N/A'}</span>
                          {room.floor != null ? <span>{` • Tầng ${room.floor}`}</span> : null}
                        </div>
                        {room.code === 'STORE' ? (
                          <div className="mt-2">
                            <span className="badge rounded-pill bg-secondary">Phòng hệ thống</span>
                          </div>
                        ) : null}
                      </div>
                      <div className="card-body pt-0">
                        {room.code !== 'STORE' ? (
                          <button className="btn btn-outline-danger btn-sm w-100" onClick={() => deleteRoom.mutate(room.id)}>
                            <i className="bi bi-trash me-1"></i>
                            Xóa
                          </button>
                        ) : (
                          <div className="text-muted small">Store Room chỉ được hệ thống sử dụng khi tạo mới hoặc checkout.</div>
                        )}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            <div className="col-lg-4">
              <div className="card h-100">
                <div className="card-header">
                  <span className="card-title">
                    <i className="bi bi-plus-circle"></i>
                    Thêm phòng mới
                  </span>
                </div>
                <div className="card-body">
                  <div className="row g-2 mb-3">
                    <div className="col-md-4">
                      <label className="form-label">Mã phòng</label>
                      <input className="form-control" value={roomForm.code} onChange={(event) => setRoomForm({ ...roomForm, code: event.target.value })} />
                    </div>
                    <div className="col-md-8">
                      <label className="form-label">Tên phòng</label>
                      <input className="form-control" value={roomForm.name} onChange={(event) => setRoomForm({ ...roomForm, name: event.target.value })} />
                    </div>
                    <div className="col-md-4">
                      <label className="form-label">Tòa nhà</label>
                      <input className="form-control" value={roomForm.building} onChange={(event) => setRoomForm({ ...roomForm, building: event.target.value })} />
                    </div>
                    <div className="col-md-4">
                      <label className="form-label">Tầng</label>
                      <input className="form-control" value={roomForm.floor} onChange={(event) => setRoomForm({ ...roomForm, floor: event.target.value })} />
                    </div>
                    <div className="col-md-4">
                      <label className="form-label">Sức chứa</label>
                      <input className="form-control" value={roomForm.capacity} onChange={(event) => setRoomForm({ ...roomForm, capacity: event.target.value })} />
                    </div>
                    <div className="col-12">
                      <label className="form-label">Mô tả</label>
                      <textarea className="form-control" rows={3} value={roomForm.description} onChange={(event) => setRoomForm({ ...roomForm, description: event.target.value })} />
                    </div>
                  </div>
                  {createRoom.error ? <div className="toast mb-3">{createRoom.error.message}</div> : null}
                  <button className="btn btn-primary w-100" onClick={() => createRoom.mutate()} disabled={createRoom.isPending || !roomForm.code || !roomForm.name}>
                    <i className="bi bi-check-lg me-1"></i>
                    Thêm phòng
                  </button>
                </div>
              </div>
            </div>
          </div>
        </>
      ) : null}

    </>
  )
}
