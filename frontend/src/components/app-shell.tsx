import { useState } from 'react'
import type { ReactNode } from 'react'
import { NavLink } from '@/lib/router'
import {
  Car,
  ChartColumn,
  ClipboardList,
  LayoutGrid,
  LogIn,
  LogOut,
  Menu,
  X,
} from 'lucide-react'
import { useAuth } from '@/auth/useAuth'
import { Button } from '@/components/ui/button'
import { cn } from '@/lib/utils'

const links = [
  { to: '/', label: 'Lot map', icon: LayoutGrid, end: true },
  { to: '/vehicles', label: 'Vehicles', icon: Car },
  { to: '/check-in', label: 'Check in', icon: LogIn },
  { to: '/check-out', label: 'Check out', icon: LogOut },
  { to: '/history', label: 'History', icon: ClipboardList },
  { to: '/reports', label: 'Reports', icon: ChartColumn, adminOnly: true },
] as const

export function AppShell({ children }: { children: ReactNode }) {
  const { user, logout } = useAuth()
  const [open, setOpen] = useState(false)

  if (!user) {
    return null
  }

  const brand = (
    <div className="flex items-center gap-2.5">
      <img
        src="/dit-logo-sm.png"
        alt="DIT"
        className="size-9 object-contain"
        width={36}
        height={36}
      />
      <div className="min-w-0">
        <p className="font-mono text-[10px] tracking-[0.14em] text-muted-foreground uppercase">
          Dar es Salaam Institute of Technology
        </p>
        <p className="truncate text-sm font-bold tracking-tight">DIT Parking Manager</p>
      </div>
    </div>
  )

  const nav = (
    <nav className="flex flex-1 flex-col gap-0.5 px-2 py-3">
      {links
        .filter((link) => !('adminOnly' in link && link.adminOnly) || user.role === 'ADMIN')
        .map((link) => (
          <NavLink
            key={link.to}
            to={link.to}
            end={'end' in link ? link.end : false}
            onClick={() => setOpen(false)}
            className={({ isActive }) =>
              cn(
                'flex items-center gap-2.5 border-l-2 px-3 py-2 text-sm transition-colors',
                isActive
                  ? 'border-destructive bg-sidebar-accent font-semibold text-sidebar-foreground'
                  : 'border-transparent text-muted-foreground hover:bg-sidebar-accent/70 hover:text-foreground',
              )
            }
          >
            <link.icon className="size-4 shrink-0" strokeWidth={1.75} />
            {link.label}
          </NavLink>
        ))}
    </nav>
  )

  return (
    <div className="flex min-h-dvh">
      <aside className="border-sidebar-border bg-sidebar text-sidebar-foreground sticky top-0 hidden h-dvh w-60 shrink-0 flex-col border-r md:flex">
        <div className="border-sidebar-border border-b px-3 py-4">{brand}</div>
        {nav}
        <div className="border-sidebar-border mt-auto space-y-2 border-t px-3 py-3">
          <div>
            <p className="text-sm font-medium">{user.fullName}</p>
            <p className="font-mono text-[11px] tracking-wide text-muted-foreground uppercase">
              {user.role}
              {user.disabilityPermit ? ' · PERMIT' : ''}
            </p>
          </div>
          <Button variant="outline" size="sm" className="w-full" onClick={() => void logout()}>
            Sign out
          </Button>
        </div>
      </aside>

      {open && (
        <div className="fixed inset-0 z-50 md:hidden">
          <button
            type="button"
            className="absolute inset-0 bg-black/30"
            aria-label="Close menu"
            onClick={() => setOpen(false)}
          />
          <aside className="border-sidebar-border bg-sidebar relative flex h-full w-72 flex-col border-r shadow-lg">
            <div className="border-sidebar-border flex items-start justify-between gap-2 border-b px-3 py-3">
              {brand}
              <Button variant="ghost" size="icon" onClick={() => setOpen(false)}>
                <X className="size-4" />
              </Button>
            </div>
            {nav}
            <div className="border-sidebar-border mt-auto space-y-2 border-t px-3 py-3">
              <p className="text-sm font-medium">{user.fullName}</p>
              <Button variant="outline" size="sm" className="w-full" onClick={() => void logout()}>
                Sign out
              </Button>
            </div>
          </aside>
        </div>
      )}

      <div className="flex min-w-0 flex-1 flex-col">
        <header className="border-border bg-card sticky top-0 z-30 flex items-center gap-3 border-b px-3 py-2 md:hidden">
          <Button variant="ghost" size="icon" onClick={() => setOpen(true)}>
            <Menu className="size-4" />
          </Button>
          <img src="/dit-logo-sm.png" alt="DIT" className="size-7 object-contain" width={28} height={28} />
          <p className="truncate text-sm font-bold tracking-tight">DIT Parking Manager</p>
        </header>
        <main className="flex-1 px-4 py-5 sm:px-6 sm:py-6 lg:px-8">
          <div className="mx-auto max-w-5xl">
            {children}
          </div>
        </main>
      </div>
    </div>
  )
}
