import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { AlertCircle, CheckCircle2, Ticket } from 'lucide-react'
import { api, ApiError } from '@/api/client'
import type { SessionResponse, SlotResponse, VehicleResponse } from '@/api/types'
import { PageHeader } from '@/components/page-header'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Label } from '@/components/ui/label'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'

export function CheckInPage() {
  const [vehicles, setVehicles] = useState<VehicleResponse[]>([])
  const [slots, setSlots] = useState<SlotResponse[]>([])
  const [plateNumber, setPlateNumber] = useState('')
  const [slotCode, setSlotCode] = useState('auto')
  const [issued, setIssued] = useState<SessionResponse | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    api.get<VehicleResponse[]>('/api/vehicles').then(setVehicles).catch(() => setVehicles([]))
    api.get<SlotResponse[]>('/api/slots').then(setSlots).catch(() => setSlots([]))
  }, [issued])

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    setIssued(null)
    setBusy(true)
    try {
      const session = await api.post<SessionResponse>('/api/sessions/check-in', {
        plateNumber,
        slotCode: slotCode === 'auto' ? null : slotCode,
      })
      setIssued(session)
      setSlotCode('auto')
    } catch (caught: unknown) {
      setError(caught instanceof ApiError ? caught.message : 'Check-in failed.')
    } finally {
      setBusy(false)
    }
  }

  const freeSlots = slots.filter((slot) => !slot.occupied)

  return (
    <>
      <PageHeader
        title="Check in"
        description="Assign a bay and issue a ticket."
      />

      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Entry</CardTitle>
            <CardDescription>Vehicle + optional bay.</CardDescription>
          </CardHeader>
          <CardContent>
            <form className="space-y-4" onSubmit={handleSubmit}>
              <div className="space-y-2">
                <Label>Vehicle</Label>
                <Select value={plateNumber || undefined} onValueChange={setPlateNumber}>
                  <SelectTrigger>
                    <SelectValue placeholder="Select a registered plate…" />
                  </SelectTrigger>
                  <SelectContent>
                    {vehicles.map((vehicle) => (
                      <SelectItem key={vehicle.id} value={vehicle.plateNumber}>
                        {vehicle.plateNumber} — {vehicle.type} ({vehicle.ownerName})
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                {vehicles.length === 0 && (
                  <p className="text-muted-foreground text-xs">
                    No vehicles yet. <Link className="text-primary underline" to="/vehicles">Register one first</Link>.
                  </p>
                )}
              </div>

              <div className="space-y-2">
                <Label>Slot</Label>
                <Select value={slotCode} onValueChange={setSlotCode}>
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="auto">Automatic — first free slot that fits</SelectItem>
                    {freeSlots.map((slot) => (
                      <SelectItem key={slot.id} value={slot.code}>
                        {slot.code} — {slot.type} / {slot.size}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              {error && (
                <Alert variant="destructive">
                  <AlertCircle />
                  <AlertTitle>Check-in failed</AlertTitle>
                  <AlertDescription>{error}</AlertDescription>
                </Alert>
              )}

              <Button type="submit" disabled={busy || !plateNumber} className="w-full">
                {busy ? 'Checking in…' : 'Check in'}
              </Button>
            </form>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Ticket</CardTitle>
            <CardDescription>Issued on success.</CardDescription>
          </CardHeader>
          <CardContent>
            {!issued ? (
              <div className="text-muted-foreground flex flex-col items-center gap-3 rounded-xl border border-dashed px-6 py-12 text-center">
                <Ticket className="size-8 opacity-50" />
                <p className="text-sm">No ticket yet. Complete check-in on the left.</p>
              </div>
            ) : (
              <div className="space-y-4">
                <Alert variant="success">
                  <CheckCircle2 />
                  <AlertTitle>Vehicle parked</AlertTitle>
                  <AlertDescription>
                    {issued.plateNumber} is in {issued.slotCode}. When they leave, open Check out & pay.
                  </AlertDescription>
                </Alert>
                <dl className="grid gap-3 text-sm sm:grid-cols-2">
                  <Info label="Ticket" value={issued.ticketCode ?? '—'} mono />
                  <Info label="Vehicle" value={issued.plateNumber} mono />
                  <Info label="Slot" value={issued.slotCode} mono />
                  <Info label="Operator" value={issued.operatorName} />
                  <Info label="Entry" value={new Date(issued.entryTime).toLocaleString()} />
                </dl>
                <Button asChild variant="outline" className="w-full">
                  <Link to="/check-out">Go to check out</Link>
                </Button>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </>
  )
}

function Info({ label, value, mono }: { label: string; value: string; mono?: boolean }) {
  return (
    <div className="rounded-lg border bg-muted/40 px-3 py-2">
      <dt className="text-muted-foreground text-xs">{label}</dt>
      <dd className={mono ? 'mt-1 font-mono font-medium' : 'mt-1 font-medium'}>{value}</dd>
    </div>
  )
}
