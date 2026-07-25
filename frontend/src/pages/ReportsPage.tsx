import { useEffect, useState } from 'react'
import { AlertCircle } from 'lucide-react'
import { api, ApiError } from '@/api/client'
import type { OccupancyReport, RevenueReport } from '@/api/types'
import { PageHeader } from '@/components/page-header'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { formatTsh } from '@/lib/utils'

export function ReportsPage() {
  const [occupancy, setOccupancy] = useState<OccupancyReport | null>(null)
  const [revenue, setRevenue] = useState<RevenueReport | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    Promise.all([
      api.get<OccupancyReport>('/api/reports/occupancy'),
      api.get<RevenueReport>('/api/reports/revenue'),
    ])
      .then(([occupancyReport, revenueReport]) => {
        setOccupancy(occupancyReport)
        setRevenue(revenueReport)
      })
      .catch((caught: unknown) =>
        setError(caught instanceof ApiError ? caught.message : 'Could not load reports.'),
      )
  }, [])

  return (
    <>
      <PageHeader
        title="Reports"
        description="Occupancy now, revenue last 30 days."
      />

      {error && (
        <Alert variant="destructive" className="mb-6">
          <AlertCircle />
          <AlertTitle>Could not load reports</AlertTitle>
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      <div className="grid gap-6 lg:grid-cols-2">
        {occupancy && (
          <Card>
            <CardHeader>
              <CardTitle className="text-base">Occupancy</CardTitle>
              <CardDescription>Live snapshot of the lot right now.</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-2 gap-3">
                <Metric label="Total slots" value={String(occupancy.totalSlots)} />
                <Metric label="Occupied" value={String(occupancy.occupiedSlots)} />
                <Metric label="Free" value={String(occupancy.freeSlots)} />
                <Metric label="Utilisation" value={`${occupancy.occupancyPercent}%`} />
                <Metric
                  label="Vehicles on site"
                  value={String(occupancy.vehiclesOnSite)}
                  className="col-span-2"
                />
              </div>
            </CardContent>
          </Card>
        )}

        {revenue && (
          <Card>
            <CardHeader>
              <CardTitle className="text-base">Revenue</CardTitle>
              <CardDescription>
                {new Date(revenue.from).toLocaleDateString()} — {new Date(revenue.to).toLocaleDateString()}
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-2 gap-3">
                <Metric label="Collected" value={formatTsh(revenue.total)} className="col-span-2" />
                <Metric
                  label="Completed sessions"
                  value={String(revenue.closedSessions)}
                  className="col-span-2"
                />
              </div>
            </CardContent>
          </Card>
        )}
      </div>
    </>
  )
}

function Metric({
  label,
  value,
  className,
}: {
  label: string
  value: string
  className?: string
}) {
  return (
    <div className={`rounded-lg border bg-muted/40 px-3 py-3 ${className ?? ''}`}>
      <p className="text-muted-foreground text-xs font-medium tracking-wide uppercase">{label}</p>
      <p className="mt-1 font-mono text-xl font-semibold tabular-nums">{value}</p>
    </div>
  )
}
