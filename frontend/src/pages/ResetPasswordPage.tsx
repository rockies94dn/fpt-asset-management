import { useMutation, useQuery } from '@tanstack/react-query'
import { type FormEvent, useEffect, useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { api } from '../api/client'

export function ResetPasswordPage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token')?.trim() ?? ''
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)

  useEffect(() => {
    document.body.classList.add('login-wrapper')
    return () => document.body.classList.remove('login-wrapper')
  }, [])

  const tokenValidation = useQuery({
    queryKey: ['reset-token', token],
    queryFn: () => api.validateResetToken(token),
    enabled: token.length > 0,
    retry: false,
  })

  const resetPassword = useMutation({
    mutationFn: () => api.confirmResetPassword({ token, password, confirmPassword }),
    onSuccess: () => {
      navigate('/login?reset=success', { replace: true })
    },
  })

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!token || !password.trim() || !confirmPassword.trim()) return
    resetPassword.mutate()
  }

  const invalidToken = !token
    || tokenValidation.data?.valid === false
    || Boolean(tokenValidation.error)

  return (
    <div className="container-fluid">
      <div className="row g-0 justify-content-center align-items-center" style={{ minHeight: '100vh', padding: '24px 16px' }}>
        <div className="col-12 col-sm-9 col-md-6 col-lg-5" style={{ maxWidth: '460px' }}>
          <div className="login-card">
            <div className="login-logo">
              <img src="/images/FPolyLogo1.png" alt="FPT Polytechnic" style={{ width: '170px', maxWidth: '100%' }} />
              <div className="login-title mt-2">Đặt lại mật khẩu</div>
              <div className="login-subtitle">Tạo mật khẩu mới cho tài khoản của bạn</div>
            </div>

            {tokenValidation.isLoading ? (
              <div className="alert alert-secondary py-2 px-3 mb-3" style={{ borderRadius: '10px', fontSize: '13px' }}>
                <i className="bi bi-hourglass-split me-2"></i>
                Đang kiểm tra liên kết đặt lại mật khẩu...
              </div>
            ) : null}

            {invalidToken ? (
              <div className="alert alert-warning py-2 px-3 mb-3" style={{ borderRadius: '10px', fontSize: '13px' }}>
                <i className="bi bi-exclamation-circle me-2"></i>
                {tokenValidation.data?.message ?? 'Liên kết đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.'}
              </div>
            ) : null}

            {resetPassword.error ? (
              <div className="alert alert-danger py-2 px-3 mb-3" style={{ borderRadius: '10px', fontSize: '13px' }}>
                <i className="bi bi-exclamation-triangle me-2"></i>
                {resetPassword.error.message}
              </div>
            ) : null}

            {!invalidToken ? (
              <form onSubmit={handleSubmit}>
                <div className="mb-3">
                  <label className="form-label">Mật khẩu mới</label>
                  <div className="input-group">
                    <span className="input-group-text" style={{ background: 'white', borderRight: 0, borderRadius: '8px 0 0 8px' }}>
                      <i className="bi bi-lock" style={{ color: '#FF6B00' }}></i>
                    </span>
                    <input
                      type={showPassword ? 'text' : 'password'}
                      className="form-control"
                      style={{ borderLeft: 0, borderRight: 0, borderRadius: 0 }}
                      placeholder="Nhập mật khẩu mới"
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
                </div>

                <div className="mb-4">
                  <label className="form-label">Xác nhận mật khẩu</label>
                  <input
                    type={showPassword ? 'text' : 'password'}
                    className="form-control"
                    style={{ borderRadius: '8px' }}
                    placeholder="Nhập lại mật khẩu mới"
                    value={confirmPassword}
                    onChange={(event) => setConfirmPassword(event.target.value)}
                    required
                  />
                </div>

                <button
                  type="submit"
                  className="btn btn-primary w-100 py-2"
                  style={{ fontSize: '15px', fontWeight: 600 }}
                  disabled={resetPassword.isPending}
                >
                  <i className="bi bi-shield-lock me-2"></i>
                  {resetPassword.isPending ? 'Đang cập nhật...' : 'Cập nhật mật khẩu'}
                </button>
              </form>
            ) : null}

            <hr style={{ margin: '20px 0', borderColor: '#f0f0f0' }} />
            <div className="text-center" style={{ fontSize: '13px', color: '#6b7280' }}>
              <Link to="/forgot-password" style={{ color: '#FF6B00', fontWeight: 600, textDecoration: 'none' }}>
                Yêu cầu liên kết mới
              </Link>
              {' · '}
              <Link to="/login" style={{ color: '#FF6B00', fontWeight: 600, textDecoration: 'none' }}>
                Quay lại đăng nhập
              </Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
