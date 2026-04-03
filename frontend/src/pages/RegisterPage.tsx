import { useMutation } from '@tanstack/react-query'
import { type FormEvent, useEffect, useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { api } from '../api/client'
import { useSession } from '../hooks/useSession'

export function RegisterPage() {
  const navigate = useNavigate()
  const session = useSession()
  const [fullName, setFullName] = useState('')
  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [phone, setPhone] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')

  useEffect(() => {
    document.body.classList.add('login-wrapper')
    return () => document.body.classList.remove('login-wrapper')
  }, [])

  const register = useMutation({
    mutationFn: () =>
      api.register({
        fullName,
        username,
        email,
        phone,
        password,
        confirmPassword,
      }),
    onSuccess: async () => {
      navigate('/login?registered=success', { replace: true })
    },
  })

  if (session.data?.authenticated) {
    return <Navigate to="/" replace />
  }

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!fullName.trim() || !username.trim() || !email.trim() || !password.trim() || !confirmPassword.trim()) {
      return
    }
    register.mutate()
  }

  return (
    <div className="container-fluid">
      <div className="row g-0 justify-content-center align-items-center" style={{ minHeight: '100vh', padding: '24px 16px' }}>
        <div className="col-12 col-sm-9 col-md-7 col-lg-6" style={{ maxWidth: '520px' }}>
          <div className="login-card">
            <div className="login-logo">
              <img src="/images/FPolyLogo1.png" alt="FPT Polytechnic" style={{ width: '170px', maxWidth: '100%' }} />
              <div className="login-title mt-2">Tạo tài khoản</div>
              <div className="login-subtitle">Đăng ký tài khoản mới để sử dụng hệ thống</div>
            </div>

            {register.error ? (
              <div className="alert alert-danger py-2 px-3 mb-3" style={{ borderRadius: '10px', fontSize: '13px' }}>
                <i className="bi bi-exclamation-triangle me-2"></i>
                {register.error.message}
              </div>
            ) : null}

            <form onSubmit={handleSubmit}>
              <div className="row g-3">
                <div className="col-12">
                  <label className="form-label">Họ và tên</label>
                  <input className="form-control" value={fullName} onChange={(event) => setFullName(event.target.value)} required />
                </div>
                <div className="col-md-6">
                  <label className="form-label">Tên đăng nhập</label>
                  <input className="form-control" value={username} onChange={(event) => setUsername(event.target.value)} required />
                </div>
                <div className="col-md-6">
                  <label className="form-label">Số điện thoại</label>
                  <input className="form-control" value={phone} onChange={(event) => setPhone(event.target.value)} />
                </div>
                <div className="col-12">
                  <label className="form-label">Email</label>
                  <input type="email" className="form-control" value={email} onChange={(event) => setEmail(event.target.value)} required />
                </div>
                <div className="col-md-6">
                  <label className="form-label">Mật khẩu</label>
                  <input type="password" className="form-control" value={password} onChange={(event) => setPassword(event.target.value)} required />
                </div>
                <div className="col-md-6">
                  <label className="form-label">Xác nhận mật khẩu</label>
                  <input type="password" className="form-control" value={confirmPassword} onChange={(event) => setConfirmPassword(event.target.value)} required />
                </div>
              </div>

              <div className="alert alert-warning py-2 px-3 mt-3 mb-3" style={{ borderRadius: '10px', fontSize: '13px' }}>
                <i className="bi bi-envelope-check me-2"></i>
                Sau khi đăng ký, bạn cần xác minh email trước khi đăng nhập.
              </div>

              <button type="submit" className="btn btn-primary w-100 py-2" style={{ fontSize: '15px', fontWeight: 600 }} disabled={register.isPending}>
                <i className="bi bi-person-plus me-2"></i>
                {register.isPending ? 'Đang tạo tài khoản...' : 'Đăng ký'}
              </button>
            </form>

            <hr style={{ margin: '20px 0', borderColor: '#f0f0f0' }} />
            <div className="text-center" style={{ fontSize: '13px', color: '#6b7280' }}>
              Đã có tài khoản?{' '}
              <Link to="/login" style={{ color: '#FF6B00', fontWeight: 600, textDecoration: 'none' }}>
                Đăng nhập
              </Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
