import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useMemo, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { api } from '../api/client'

const issueTypeOptions = [
  { value: '', label: 'Tất cả loại ticket' },
  { value: 'BROKEN', label: 'Hỏng' },
  { value: 'MAINTENANCE', label: 'Bảo trì' },
  { value: 'UPGRADE', label: 'Nâng cấp' },
]

function issueTypeLabel(value: string | null) {
  return issueTypeOptions.find((option) => option.value === (value ?? ''))?.label ?? (value || 'Tất cả loại ticket')
}

export function AdminPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const queryClient = useQueryClient()
  const users = useQuery({ queryKey: ['admin', 'users'], queryFn: api.adminUsers })
  const adminMeta = useQuery({ queryKey: ['admin', 'meta'], queryFn: api.adminMeta })
  const rooms = useQuery({ queryKey: ['admin', 'rooms'], queryFn: api.adminRooms })
  const coverageRules = useQuery({ queryKey: ['admin', 'coverage-rules'], queryFn: api.coverageRules })

  const [roomForm, setRoomForm] = useState({ code: '', name: '', building: '', floor: '', capacity: '', description: '' })
  const [roomNotice, setRoomNotice] = useState<{ tone: 'success' | 'danger'; message: string } | null>(null)
  const [coverageForm, setCoverageForm] = useState({
    technicianId: '',
    categoryId: '',
    issueType: '',
  })
  const [coverageNotice, setCoverageNotice] = useState<{ tone: 'success' | 'danger'; message: string } | null>(null)
  const [editingUserId, setEditingUserId] = useState<number | null>(null)
  const [userForm, setUserForm] = useState({
    fullName: '',
    username: '',
    email: '',
    phone: '',
    roleId: '',
  })

  const createRoom = useMutation({
    mutationFn: () =>
      api.createRoom({
        ...roomForm,
        floor: roomForm.floor ? Number(roomForm.floor) : null,
        capacity: roomForm.capacity ? Number(roomForm.capacity) : null,
      }),
    onSuccess: async () => {
      setRoomForm({ code: '', name: '', building: '', floor: '', capacity: '', description: '' })
      setRoomNotice({ tone: 'success', message: 'Đã tạo phòng mới.' })
      await queryClient.invalidateQueries({ queryKey: ['admin', 'rooms'] })
      await queryClient.invalidateQueries({ queryKey: ['admin', 'meta'] })
    },
    onError: (error: Error) => {
      setRoomNotice({ tone: 'danger', message: error.message })
    },
  })

  const deleteRoom = useMutation({
    mutationFn: (id: number) => api.deleteRoom(id),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['admin', 'rooms'] })
      await queryClient.invalidateQueries({ queryKey: ['admin', 'meta'] })
    },
  })

  const createCoverageRule = useMutation({
    mutationFn: () =>
      api.createCoverageRule({
        technicianId: Number(coverageForm.technicianId),
        categoryId: Number(coverageForm.categoryId),
        roomId: null,
        issueType: coverageForm.issueType || null,
        sortOrder: 0,
        active: true,
      }),
    onSuccess: async () => {
      setCoverageForm({ technicianId: '', categoryId: '', issueType: '' })
      setCoverageNotice({ tone: 'success', message: 'Đã lưu quy tắc phân công kỹ thuật.' })
      await queryClient.invalidateQueries({ queryKey: ['admin', 'coverage-rules'] })
    },
    onError: (error: Error) => {
      setCoverageNotice({ tone: 'danger', message: error.message })
    },
  })

  const deleteCoverageRule = useMutation({
    mutationFn: (id: number) => api.deleteCoverageRule(id),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['admin', 'coverage-rules'] })
    },
  })

  const toggleUser = useMutation({
    mutationFn: (id: number) => api.toggleUser(id),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['admin', 'users'] })
      await queryClient.invalidateQueries({ queryKey: ['admin', 'meta'] })
    },
  })

  const updateUser = useMutation({
    mutationFn: () =>
      api.updateAdminUser(editingUserId!, {
        ...userForm,
        roleId: Number(userForm.roleId),
      }),
    onSuccess: async () => {
      setEditingUserId(null)
      setUserForm({ fullName: '', username: '', email: '', phone: '', roleId: '' })
      await queryClient.invalidateQueries({ queryKey: ['admin', 'users'] })
      await queryClient.invalidateQueries({ queryKey: ['admin', 'meta'] })
    },
  })

  const activeRooms = rooms.data?.filter((room) => room.active) ?? []
  const technicians = adminMeta.data?.technicians ?? []
  const categories = adminMeta.data?.categories ?? []
  const currentTab = useMemo(() => {
    const tab = searchParams.get('tab')
    if (tab === 'rooms' || tab === 'coverage') return tab
    return 'users'
  }, [searchParams])
  const roleOptions = adminMeta.data?.roles ?? []

  function startEditUser(user: {
    id: number
    fullName: string
    username: string
    email: string | null
    phone: string | null
    role: string
  }) {
    const matchedRole = roleOptions.find((role) => role.name === user.role)
    setEditingUserId(user.id)
    setUserForm({
      fullName: user.fullName,
      username: user.username,
      email: user.email ?? '',
      phone: user.phone ?? '',
      roleId: matchedRole ? String(matchedRole.id) : '',
    })
  }

  function cancelEditUser() {
    setEditingUserId(null)
    setUserForm({ fullName: '', username: '', email: '', phone: '', roleId: '' })
    updateUser.reset()
  }

  function setTab(tab: 'users' | 'rooms' | 'coverage') {
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
            <button
              type="button"
              className={`btn ${currentTab === 'coverage' ? 'btn-primary' : 'btn-outline-primary'}`}
              onClick={() => setTab('coverage')}
            >
              <i className="bi bi-diagram-3 me-1"></i>
              Phân công kỹ thuật
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

          {editingUserId != null ? (
            <div className="card mb-3">
              <div className="card-header">
                <span className="card-title">
                  <i className="bi bi-pencil-square"></i>
                  Chỉnh sửa người dùng
                </span>
              </div>
              <div className="card-body">
                <div className="row g-3 mb-3">
                  <div className="col-md-6">
                    <label className="form-label">Họ tên</label>
                    <input
                      className="form-control"
                      value={userForm.fullName}
                      onChange={(event) => setUserForm({ ...userForm, fullName: event.target.value })}
                    />
                  </div>
                  <div className="col-md-6">
                    <label className="form-label">Tên đăng nhập</label>
                    <input
                      className="form-control"
                      value={userForm.username}
                      onChange={(event) => setUserForm({ ...userForm, username: event.target.value })}
                    />
                  </div>
                  <div className="col-md-6">
                    <label className="form-label">Email</label>
                    <input
                      type="email"
                      className="form-control"
                      value={userForm.email}
                      onChange={(event) => setUserForm({ ...userForm, email: event.target.value })}
                    />
                  </div>
                  <div className="col-md-6">
                    <label className="form-label">Số điện thoại</label>
                    <input
                      className="form-control"
                      value={userForm.phone}
                      onChange={(event) => setUserForm({ ...userForm, phone: event.target.value })}
                    />
                  </div>
                  <div className="col-md-6">
                    <label className="form-label">Quyền</label>
                    <select
                      className="form-select"
                      value={userForm.roleId}
                      onChange={(event) => setUserForm({ ...userForm, roleId: event.target.value })}
                    >
                      <option value="">Chọn quyền</option>
                      {roleOptions.map((role) => (
                        <option key={role.id} value={role.id}>
                          {role.name}
                        </option>
                      ))}
                    </select>
                  </div>
                </div>

                {updateUser.error ? <div className="toast mb-3">{updateUser.error.message}</div> : null}

                <div className="d-flex flex-wrap gap-2">
                  <button
                    className="btn btn-primary"
                    onClick={() => updateUser.mutate()}
                    disabled={updateUser.isPending || !userForm.fullName || !userForm.username || !userForm.roleId}
                  >
                    <i className="bi bi-check-lg me-1"></i>
                    {updateUser.isPending ? 'Đang lưu...' : 'Lưu thay đổi'}
                  </button>
                  <button className="btn btn-outline-secondary" onClick={cancelEditUser} disabled={updateUser.isPending}>
                    <i className="bi bi-x-lg me-1"></i>
                    Hủy
                  </button>
                </div>
              </div>
            </div>
          ) : null}

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
                        <button className="btn btn-sm btn-outline-primary" onClick={() => startEditUser(user)}>
                          <i className="bi bi-pencil-square me-1"></i>
                          Sửa
                        </button>
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
                      <input
                        className="form-control"
                        value={roomForm.code}
                        onChange={(event) => {
                          setRoomNotice(null)
                          setRoomForm({ ...roomForm, code: event.target.value })
                        }}
                      />
                    </div>
                    <div className="col-md-8">
                      <label className="form-label">Tên phòng</label>
                      <input
                        className="form-control"
                        value={roomForm.name}
                        onChange={(event) => {
                          setRoomNotice(null)
                          setRoomForm({ ...roomForm, name: event.target.value })
                        }}
                      />
                    </div>
                    <div className="col-md-4">
                      <label className="form-label">Tòa nhà</label>
                      <input
                        className="form-control"
                        value={roomForm.building}
                        onChange={(event) => {
                          setRoomNotice(null)
                          setRoomForm({ ...roomForm, building: event.target.value })
                        }}
                      />
                    </div>
                    <div className="col-md-4">
                      <label className="form-label">Tầng</label>
                      <input
                        className="form-control"
                        value={roomForm.floor}
                        onChange={(event) => {
                          setRoomNotice(null)
                          setRoomForm({ ...roomForm, floor: event.target.value })
                        }}
                      />
                    </div>
                    <div className="col-md-4">
                      <label className="form-label">Sức chứa</label>
                      <input
                        className="form-control"
                        value={roomForm.capacity}
                        onChange={(event) => {
                          setRoomNotice(null)
                          setRoomForm({ ...roomForm, capacity: event.target.value })
                        }}
                      />
                    </div>
                    <div className="col-12">
                      <label className="form-label">Mô tả</label>
                      <textarea
                        className="form-control"
                        rows={3}
                        value={roomForm.description}
                        onChange={(event) => {
                          setRoomNotice(null)
                          setRoomForm({ ...roomForm, description: event.target.value })
                        }}
                      />
                    </div>
                  </div>
                  {roomNotice ? (
                    <div
                      className={`alert alert-${roomNotice.tone} py-2 px-3 mb-3`}
                      style={{ borderRadius: '10px', fontSize: '13px' }}
                      role="alert"
                    >
                      {roomNotice.message}
                    </div>
                  ) : null}
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

      {currentTab === 'coverage' ? (
        <>
          <div className="page-header mb-3" style={{ paddingBottom: 0 }}>
            <div className="page-title" style={{ fontSize: '22px' }}>
              <i className="bi bi-diagram-3"></i>
              Phân công kỹ thuật theo category
            </div>
          </div>

          <div className="row g-3 mb-4">
            <div className="col-lg-4">
              <div className="card h-100">
                <div className="card-header">
                  <span className="card-title">
                    <i className="bi bi-plus-circle"></i>
                    Thêm quy tắc
                  </span>
                </div>
                <div className="card-body">
                  <div className="row g-3 mb-3">
                    <div className="col-12">
                      <label className="form-label">Kỹ thuật viên</label>
                      <select
                        className="form-select"
                        value={coverageForm.technicianId}
                        onChange={(event) => {
                          setCoverageNotice(null)
                          setCoverageForm({ ...coverageForm, technicianId: event.target.value })
                        }}
                      >
                        <option value="">Chọn kỹ thuật viên</option>
                        {technicians.map((technician) => (
                          <option key={technician.id} value={technician.id}>
                            {technician.fullName} ({technician.username})
                          </option>
                        ))}
                      </select>
                    </div>
                    <div className="col-12">
                      <label className="form-label">Category thiết bị</label>
                      <select
                        className="form-select"
                        value={coverageForm.categoryId}
                        onChange={(event) => {
                          setCoverageNotice(null)
                          setCoverageForm({ ...coverageForm, categoryId: event.target.value })
                        }}
                      >
                        <option value="">Chọn category</option>
                        {categories.map((category) => (
                          <option key={category.id} value={category.id}>
                            {category.name}
                          </option>
                        ))}
                      </select>
                    </div>
                    <div className="col-12">
                      <label className="form-label">Loại ticket</label>
                      <select
                        className="form-select"
                        value={coverageForm.issueType}
                        onChange={(event) => {
                          setCoverageNotice(null)
                          setCoverageForm({ ...coverageForm, issueType: event.target.value })
                        }}
                      >
                        {issueTypeOptions.map((option) => (
                          <option key={option.value || 'ALL'} value={option.value}>
                            {option.label}
                          </option>
                        ))}
                      </select>
                    </div>
                  </div>

                  {coverageNotice ? (
                    <div
                      className={`alert alert-${coverageNotice.tone} py-2 px-3 mb-3`}
                      style={{ borderRadius: '10px', fontSize: '13px' }}
                      role="alert"
                    >
                      {coverageNotice.message}
                    </div>
                  ) : null}

                  <button
                    className="btn btn-primary w-100"
                    onClick={() => createCoverageRule.mutate()}
                    disabled={createCoverageRule.isPending || !coverageForm.technicianId || !coverageForm.categoryId}
                  >
                    <i className="bi bi-check-lg me-1"></i>
                    {createCoverageRule.isPending ? 'Đang lưu...' : 'Lưu quy tắc'}
                  </button>
                </div>
              </div>
            </div>

            <div className="col-lg-8">
              <div className="card h-100">
                <div className="card-header">
                  <span className="card-title">
                    <i className="bi bi-list-task"></i>
                    Quy tắc hiện có
                  </span>
                </div>
                <div className="card-body p-0">
                  <div className="table-responsive">
                    <table className="table table-hover mb-0">
                      <thead>
                        <tr>
                          <th>Kỹ thuật viên</th>
                          <th>Category</th>
                          <th>Loại ticket</th>
                          <th>Thao tác</th>
                        </tr>
                      </thead>
                      <tbody>
                        {coverageRules.data?.length ? (
                          coverageRules.data.map((rule) => (
                            <tr key={rule.id}>
                              <td>
                                <div style={{ fontWeight: 600 }}>{rule.technician.fullName}</div>
                                <div style={{ fontSize: '12px', color: '#6b7280' }}>{rule.technician.username}</div>
                              </td>
                              <td>{rule.category?.name ?? 'Tất cả category'}</td>
                              <td>{issueTypeLabel(rule.issueType)}</td>
                              <td className="react-table-actions">
                                <button className="btn btn-sm btn-outline-danger" onClick={() => deleteCoverageRule.mutate(rule.id)} disabled={deleteCoverageRule.isPending}>
                                  <i className="bi bi-trash me-1"></i>
                                  Xóa
                                </button>
                              </td>
                            </tr>
                          ))
                        ) : (
                          <tr>
                            <td colSpan={4}>
                              <div className="empty-state py-4">
                                <div className="empty-state-icon">
                                  <i className="bi bi-diagram-3"></i>
                                </div>
                                <div className="empty-state-text">Chưa có quy tắc phân công nào</div>
                              </div>
                            </td>
                          </tr>
                        )}
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </>
      ) : null}
    </>
  )
}
