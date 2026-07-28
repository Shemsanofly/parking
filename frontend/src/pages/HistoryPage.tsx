import { useEffect, useState } from 'react'
import { AlertCircle, History } from 'lucide-react'
import { api, ApiError } from '@/api/client'
import { useAuth } from '@/auth/useAuth'
import type { SessionResponse } from '@/api/types'
import { EmptyState } from '@/components/empty-state'
import { PageHeader } from '@/components/page-header'
import { SessionStatusBadge } from '@/components/session-status-badge'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { formatTsh } from '@/lib/utils'

export function HistoryPage() {
  const { user } = useAuth()
  const [sessions, setSessions] = useState<SessionResponse[]>([])
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    api
      .get<SessionResponse[]>('/api/sessions')
      .then(setSessions)
      .catch((caught: unknown) =>
        setError(caught instanceof ApiError ? caught.message : 'Could not load history.'),
      )
  }, [])

  const total = sessions
    .filter((session) => session.status === 'CLOSED')
    .reduce((sum, session) => sum + Number(session.fee ?? 0), 0)

  return (
    <>
      <PageHeader
        title={user?.role === 'ADMIN' ? 'History' : 'My history'}
        description="Newest sessions first."
      />

      {error && (
        <Alert variant="destructive" className="mb-6">
          <AlertCircle />
          <AlertTitle>Could not load history</AlertTitle>
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      <div className="mb-6 grid gap-3 sm:grid-cols-2">
        <Card className="py-4">
          <CardContent>
            <p className="text-muted-foreground text-xs font-medium tracking-wide uppercase">Sessions</p>
            <p className="mt-1 font-mono text-2xl font-semibold tabular-nums">{sessions.length}</p>
          </CardContent>
        </Card>
        <Card className="py-4">
          <CardContent>
            <p className="text-muted-foreground text-xs font-medium tracking-wide uppercase">Paid total</p>
            <p className="mt-1 font-mono text-2xl font-semibold tabular-nums">{formatTsh(total)}</p>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">Sessions</CardTitle>
        </CardHeader>
        <CardContent>
          {sessions.length === 0 ? (
            <EmptyState
              icon={History}
              title="No sessions yet"
              description="History fills in after you check vehicles in and out."
            />
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="text-muted-foreground border-b text-left text-xs uppercase">
                    <th className="pb-2 pr-3 font-medium">Ticket</th>
                    <th className="pb-2 pr-3 font-medium">Plate</th>
                    <th className="pb-2 pr-3 font-medium">Slot</th>
                    <th className="pb-2 pr-3 font-medium">Entry</th>
                    <th className="pb-2 pr-3 font-medium">Exit</th>
                    <th className="pb-2 pr-3 font-medium">Fee</th>
                    <th className="pb-2 pr-3 font-medium">Status</th>
                    <th className="pb-2 font-medium">Operator</th>
                  </tr>
                </thead>
                <tbody>
                  {sessions.map((session) => (
                    <tr key={session.id} className="border-b last:border-0">
                      <td className="text-muted-foreground py-3 pr-3 font-mono text-xs">
                        {session.ticketCode ?? '—'}
                      </td>
                      <td className="py-3 pr-3 font-mono font-medium">{session.plateNumber}</td>
                      <td className="py-3 pr-3 font-mono">{session.slotCode}</td>
                      <td className="text-muted-foreground py-3 pr-3">
                        {new Date(session.entryTime).toLocaleString()}
                      </td>
                      <td className="text-muted-foreground py-3 pr-3">
                        {session.exitTime ? new Date(session.exitTime).toLocaleString() : '—'}
                      </td>
                      <td className="py-3 pr-3 font-mono tabular-nums">{formatTsh(session.fee)}</td>
                      <td className="py-3 pr-3">
                        <SessionStatusBadge status={session.status} />
                      </td>
                      <td className="text-muted-foreground py-3">{session.operatorName}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </CardContent>
      </Card>
    </>
  )
}
