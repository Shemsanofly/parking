import { Navigate } from 'react-router-dom'
import type { ReactNode } from 'react'
import { useAuth } from './AuthContext'

export function RequireAuth({
  children,
  adminOnly = false,
}: {
  children: ReactNode
  adminOnly?: boolean
}) {
  const { user, loading } = useAuth()

  if (loading) {
    return (
      <div className="text-muted-foreground flex min-h-40 items-center justify-center text-sm">
        Loading your session…
      </div>
    )
  }
  if (!user) {
    return <Navigate to="/login" replace />
  }
  if (adminOnly && user.role !== 'ADMIN') {
    return <Navigate to="/" replace />
  }
  return <>{children}</>
}
