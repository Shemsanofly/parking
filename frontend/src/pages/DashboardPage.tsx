import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { AlertCircle } from 'lucide-react'
import { api, ApiError } from '@/api/client'
import { useAuth } from '@/auth/AuthContext'
import type { OccupancyReport, SlotResponse } from '@/api/types'
import { PageHeader } from '@/components/page-header'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { cn } from '@/lib/utils'

export function DashboardPage() {
  const { user } = useAuth()
  const [slots, setSlots] = useState<SlotResponse[]>([])
  const [occupancy, setOccupancy] = useState<OccupancyReport | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    api
      .get<SlotResponse[]>('/api/slots')
      .then(setSlots)
      .catch((caught: unknown) =>
        setError(caught instanceof ApiError ? caught.message : 'Could not load slots.'),
      )

    if (user?.role === 'ADMIN') {
      api
        .get<OccupancyReport>('/api/reports/occupancy')
        .then(setOccupancy)
        .catch(() => setOccupancy(null))
    }
  }, [user])

  const free = slots.filter((slot) => slot.available).length
  const occupied = slots.length - free

  return (
    <>
      <PageHeader
        title="Lot map"
        description={`${free} free · ${occupied} occupied`}
        action={
          <Button asChild>
            <Link to="/check-in">Check in</Link>
          </Button>
        }
      />

      <div className="mb-5 grid grid-cols-2 gap-px border border-border bg-border sm:grid-cols-4">
        <Stat label="Slots" value={String(slots.length)} />
        <Stat label="Free" value={String(free)} />
        <Stat label="Occupied" value={String(occupied)} />
        <Stat
          label="Utilisation"
          value={occupancy ? `${occupancy.occupancyPercent}%` : '—'}
        />
      </div>

      {error && (
        <Alert variant="destructive" className="mb-5">
          <AlertCircle />
          <AlertTitle>Load failed</AlertTitle>
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      <Card>
        <CardHeader className="flex flex-row items-center justify-between gap-3 border-b border-border pb-3">
          <CardTitle>Bays</CardTitle>
          <p className="font-mono text-[10px] tracking-wide text-muted-foreground uppercase">
            Free · thick left edge
          </p>
        </CardHeader>
        <CardContent className="pt-1">
          <div className="grid grid-cols-2 gap-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5">
            {slots.map((slot) => (
              <div
                key={slot.id}
                className={cn(
                  'border border-border bg-background p-2.5',
                  slot.available ? 'border-l-[3px] border-l-success' : 'border-l-[3px] border-l-destructive',
                )}
              >
                <div className="flex items-start justify-between gap-2">
                  <p className="font-mono text-sm font-semibold">{slot.code}</p>
                  <Badge variant={slot.available ? 'outline' : 'danger'}>
                    {slot.available ? 'FREE' : 'BUSY'}
                  </Badge>
                </div>
                <p className="mt-1.5 font-mono text-[10px] tracking-wide text-muted-foreground uppercase">
                  {slot.type} / {slot.size}
                </p>
                {slot.reservedFor && (
                  <p className="mt-1 text-xs text-muted-foreground">→ {slot.reservedFor}</p>
                )}
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    </>
  )
}

function Stat({ label, value }: { label: string; value: string }) {
  return (
    <div className="bg-card px-3 py-3">
      <p className="font-mono text-[10px] tracking-wide text-muted-foreground uppercase">{label}</p>
      <p className="mt-1 font-mono text-2xl font-semibold tabular-nums">{value}</p>
    </div>
  )
}
