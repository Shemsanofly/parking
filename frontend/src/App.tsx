import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
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

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route
            element={
              <RequireAuth>
                <AppShell />
              </RequireAuth>
            }
          >
            <Route index element={<DashboardPage />} />
            <Route path="vehicles" element={<VehiclesPage />} />
            <Route path="check-in" element={<CheckInPage />} />
            <Route path="check-out" element={<CheckOutPage />} />
            <Route path="history" element={<HistoryPage />} />
            <Route
              path="reports"
              element={
                <RequireAuth adminOnly>
                  <ReportsPage />
                </RequireAuth>
              }
            />
          </Route>
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}
