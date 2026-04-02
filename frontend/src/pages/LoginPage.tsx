import { useMutation, useQueryClient } from '@tanstack/react-query'
import { type FormEvent, useEffect, useState } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { api, setCsrfToken } from '../api/client'
import { useSession } from '../hooks/useSession'

export function LoginPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const queryClient = useQueryClient()
  const session = useSession()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const searchParams = new URLSearchParams(location.search)
  const verified = searchParams.get('verified')
  const reset = searchParams.get('reset')

  useEffect(() => {
    document.body.classList.add('login-wrapper')
    return () => document.body.classList.remove('login-wrapper')
  }, [])

  const login = useMutation({
    mutationFn: () => api.login({ username, password }),
    onSuccess: async (response) => {
      setCsrfToken(response.csrfToken)
      await queryClient.invalidateQueries({ queryKey: ['session'] })
      navigate((location.state as { from?: string } | undefined)?.from ?? '/', { replace: true })
    },
  })

  if (session.data?.authenticated) {
    return <Navigate to="/" replace />
  }

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!username.trim() || !password.trim()) return
    login.mutate()
  }

  return (
    <div className="container-fluid">
      <div className="row g-0 justify-content-center align-items-center" style={{ minHeight: '100vh', padding: '24px 16px' }}>
        <div className="col-lg-6 col-md-10 d-none d-lg-flex flex-column justify-content-center login-brand-panel">
          <div
            style={{
              width: '70px',
              height: '70px',
              background: 'linear-gradient(135deg,#FF6B00,#E05A00)',
              borderRadius: '18px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '36px',
              marginBottom: '24px',
              boxShadow: '0 12px 32px rgba(255,107,0,0.4)',
            }}
          >
            <i className="bi bi-cpu-fill" style={{ color: 'white' }}></i>
          </div>

          <h1>
            FPT Asset
            <br />
            Management
          </h1>
          <p className="brand-desc">
            Hệ thống quản lý cơ sở hạ tầng
            <br />
            FPT Polytechnic Đà Nẵng
          </p>

          <div className="login-feature-list">
            <div className="login-feature-item">
              <i className="bi bi-qr-code-scan"></i>
              <div>
                <div className="feat-title">Quản lý bằng QR Code</div>
                <div className="feat-sub">Theo dõi thiết bị nhanh chóng</div>
              </div>
            </div>
            <div className="login-feature-item">
              <i className="bi bi-graph-up-arrow"></i>
              <div>
                <div className="feat-title">Thống kê trực quan</div>
                <div className="feat-sub">Dashboard real-time</div>
              </div>
            </div>
            <div className="login-feature-item">
              <i className="bi bi-file-earmark-arrow-down"></i>
              <div>
                <div className="feat-title">Xuất báo cáo Excel/PDF</div>
                <div className="feat-sub">Báo cáo theo phòng ban</div>
              </div>
            </div>
          </div>
        </div>

        <div className="col-12 col-sm-9 col-md-6 col-lg-6" style={{ maxWidth: '440px' }}>
          <div className="login-card">
            <div className="login-logo">
              <img src="/images/FPolyLogo1.png" alt="FPT Polytechnic" style={{ width: '170px', maxWidth: '100%' }} />
              <div className="login-title mt-2">Chào mừng trở lại!</div>
              <div className="login-subtitle">Đăng nhập để tiếp tục quản lý</div>
            </div>

            {login.error ? (
              <div className="alert alert-danger py-2 px-3 mb-3" style={{ borderRadius: '10px', fontSize: '13px' }}>
                <i className="bi bi-exclamation-triangle me-2"></i>
                {login.error.message}
              </div>
            ) : null}

            {verified === 'success' ? (
              <div className="alert alert-success py-2 px-3 mb-3" style={{ borderRadius: '10px', fontSize: '13px' }}>
                <i className="bi bi-check-circle me-2"></i>
                Xác minh email thành công. Bạn có thể đăng nhập ngay bây giờ.
              </div>
            ) : null}

            {verified === 'invalid' ? (
              <div className="alert alert-warning py-2 px-3 mb-3" style={{ borderRadius: '10px', fontSize: '13px' }}>
                <i className="bi bi-exclamation-circle me-2"></i>
                Liên kết xác minh không hợp lệ hoặc đã hết hạn.
              </div>
            ) : null}

            {reset === 'success' ? (
              <div className="alert alert-success py-2 px-3 mb-3" style={{ borderRadius: '10px', fontSize: '13px' }}>
                <i className="bi bi-check-circle me-2"></i>
                Đặt lại mật khẩu thành công. Hãy đăng nhập bằng mật khẩu mới.
              </div>
            ) : null}

            <form onSubmit={handleSubmit}>
              <div className="mb-3">
                <label className="form-label">Tên đăng nhập</label>
                <div className="input-group">
                  <span className="input-group-text" style={{ background: 'white', borderRight: 0, borderRadius: '8px 0 0 8px' }}>
                    <i className="bi bi-person" style={{ color: '#FF6B00' }}></i>
                  </span>
                  <input
                    type="text"
                    className="form-control"
                    style={{ borderLeft: 0, borderRadius: '0 8px 8px 0' }}
                    placeholder="Nhập tên đăng nhập"
                    value={username}
                    onChange={(event) => setUsername(event.target.value)}
                    autoFocus
                    required
                  />
                </div>
              </div>

              <div className="mb-4">
                <label className="form-label">Mật khẩu</label>
                <div className="input-group">
                  <span className="input-group-text" style={{ background: 'white', borderRight: 0, borderRadius: '8px 0 0 8px' }}>
                    <i className="bi bi-lock" style={{ color: '#FF6B00' }}></i>
                  </span>
                  <input
                    type={showPassword ? 'text' : 'password'}
                    className="form-control"
                    style={{ borderLeft: 0, borderRight: 0, borderRadius: 0 }}
                    placeholder="Nhập mật khẩu"
                    value={password}
                    onChange={(event) => setPassword(event.target.value)}
                    required
                  />
                  <button
                    type="button"
                    className="input-group-text"
                    style={{ background: 'white', cursor: 'pointer', borderRadius: '0 8px 8px 0' }}
                    onClick={() => setShowPassword((value) => !value)}
                  >
                    <i className={`bi ${showPassword ? 'bi-eye-slash' : 'bi-eye'}`}></i>
                  </button>
                </div>
                <div className="mt-2" style={{ color: '#6b7280', fontSize: '13px' }}>
                  <Link to="/forgot-password" style={{ color: '#FF6B00', fontWeight: 600, textDecoration: 'none' }}>
                    Quên mật khẩu?
                  </Link>
                  {' '}
                  Liên hệ quản trị viên nếu bạn cần tạo tài khoản mới.
                </div>
              </div>

              <button type="submit" className="btn btn-primary w-100 py-2" style={{ fontSize: '15px', fontWeight: 600 }} disabled={login.isPending}>
                <i className="bi bi-box-arrow-in-right me-2"></i>
                {login.isPending ? 'Đang đăng nhập...' : 'Đăng nhập'}
              </button>
            </form>

            <hr style={{ margin: '20px 0', borderColor: '#f0f0f0' }} />
            <div className="text-center" style={{ fontSize: '13px', color: '#6b7280' }}>
              Hệ thống hiện dùng giao diện React SPA, không còn phụ thuộc vào Thymeleaf.
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
