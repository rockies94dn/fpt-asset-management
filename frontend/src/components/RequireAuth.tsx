import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useSession } from '../hooks/useSession'

export function RequireAuth() {
  const location = useLocation()
  const session = useSession()

  if (session.isLoading) {
    return <div className="login-shell"><div className="toast">Loading session...</div></div>
  }

  if (!session.data?.authenticated) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }

  return <Outlet />
}
