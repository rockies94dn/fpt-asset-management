import { useMutation } from '@tanstack/react-query'
import { type FormEvent, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api/client'

export function ForgotPasswordPage() {
  const [email, setEmail] = useState('')
  const [submittedMessage, setSubmittedMessage] = useState<string | null>(null)

  useEffect(() => {
    document.body.classList.add('login-wrapper')
    return () => document.body.classList.remove('login-wrapper')
  }, [])

  const forgotPassword = useMutation({
    mutationFn: () => api.forgotPassword(email),
    onSuccess: (response) => {
      setSubmittedMessage(response.message)
    },
  })

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!email.trim()) return
    forgotPassword.mutate()
  }

  return (
    <div className="container-fluid">
      <div className="row g-0 justify-content-center align-items-center" style={{ minHeight: '100vh', padding: '24px 16px' }}>
        <div className="col-12 col-sm-9 col-md-6 col-lg-5" style={{ maxWidth: '440px' }}>
          <div className="login-card">
            <div className="login-logo">
              <img src="/images/FPolyLogo1.png" alt="FPT Polytechnic" style={{ width: '170px', maxWidth: '100%' }} />
              <div className="login-title mt-2">Quên mật khẩu</div>
              <div className="login-subtitle">Nhập email để nhận liên kết đặt lại mật khẩu</div>
            </div>

            {forgotPassword.error ? (
              <div className="alert alert-danger py-2 px-3 mb-3" style={{ borderRadius: '10px', fontSize: '13px' }}>
                <i className="bi bi-exclamation-triangle me-2"></i>
                {forgotPassword.error.message}
              </div>
            ) : null}

            {submittedMessage ? (
              <div className="alert alert-success py-2 px-3 mb-3" style={{ borderRadius: '10px', fontSize: '13px' }}>
                <i className="bi bi-envelope-check me-2"></i>
                {submittedMessage}
              </div>
            ) : null}

            <form onSubmit={handleSubmit}>
              <div className="mb-4">
                <label className="form-label">Email</label>
                <div className="input-group">
                  <span className="input-group-text" style={{ background: 'white', borderRight: 0, borderRadius: '8px 0 0 8px' }}>
                    <i className="bi bi-envelope" style={{ color: '#FF6B00' }}></i>
                  </span>
                  <input
                    type="email"
                    className="form-control"
                    style={{ borderLeft: 0, borderRadius: '0 8px 8px 0' }}
                    placeholder="Nhập email tài khoản"
                    value={email}
                    onChange={(event) => setEmail(event.target.value)}
                    autoFocus
                    required
                  />
                </div>
              </div>

              <button
                type="submit"
                className="btn btn-primary w-100 py-2"
                style={{ fontSize: '15px', fontWeight: 600 }}
                disabled={forgotPassword.isPending}
              >
                <i className="bi bi-send me-2"></i>
                {forgotPassword.isPending ? 'Đang gửi...' : 'Gửi liên kết đặt lại'}
              </button>
            </form>

            <hr style={{ margin: '20px 0', borderColor: '#f0f0f0' }} />
            <div className="text-center" style={{ fontSize: '13px', color: '#6b7280' }}>
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
