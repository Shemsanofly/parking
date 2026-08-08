import { BrowserRouter, Navigate } from '@/lib/router'
import { usePathname } from '@/lib/router-hooks'
import { AuthProvider } from '@/auth/AuthContext'
import { RequireAuth } from '@/auth/RequireAuth'
import { AppShell } from '@/components/app-shell'
import { LoginPage } from '@/pages/LoginPage'
import { DashboardPage } from '@/pages/DashboardPage'
import { VehiclesPage } from '@/pages/VehiclesPage'
import { CheckInPage } from '@/pages/CheckInPage'
import { CheckOutPage } from '@/pages/CheckOutPage'
import { HistoryPage } from '@/pages/HistoryPage'
import { ReportsPage } from '@/pages/ReportsPage'

function AppRoutes() {
  const path = usePathname()

  if (path === '/login') {
    return <LoginPage />
  }

  let page
  switch (path) {
    case '/':
      page = <DashboardPage />
      break
    case '/vehicles':
      page = <VehiclesPage />
      break
    case '/check-in':
      page = <CheckInPage />
      break
    case '/check-out':
      page = <CheckOutPage />
      break
    case '/history':
      page = <HistoryPage />
      break
    case '/reports':
      page = (
        <RequireAuth adminOnly>
          <ReportsPage />
        </RequireAuth>
      )
      break
    default:
      return <Navigate to="/" replace />
  }

  return (
    <RequireAuth>
      <AppShell>{page}</AppShell>
    </RequireAuth>
  )
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <AppRoutes />
      </BrowserRouter>
    </AuthProvider>
  )
}
