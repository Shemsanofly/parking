import { useCallback, useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { Link } from '@/lib/router'
import { AlertCircle, CheckCircle2, Edit3, Trash2, X } from 'lucide-react'
import { api, ApiError } from '@/api/client'
import { useAuth } from '@/auth/useAuth'
import type { OccupancyReport, SlotResponse, SlotSize, SlotType } from '@/api/types'
import { PageHeader } from '@/components/page-header'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { cn } from '@/lib/utils'

type ReservationMode = 'KEEP' | 'SET' | 'CLEAR'

export function DashboardPage() {
  const { user } = useAuth()
  const [slots, setSlots] = useState<SlotResponse[]>([])
  const [occupancy, setOccupancy] = useState<OccupancyReport | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [slotMessage, setSlotMessage] = useState<string | null>(null)
  const [slotError, setSlotError] = useState<string | null>(null)
  const [slotBusy, setSlotBusy] = useState(false)
  const [editingSlot, setEditingSlot] = useState<SlotResponse | null>(null)
  const [slotCode, setSlotCode] = useState('')
  const [slotType, setSlotType] = useState<SlotType>('STANDARD')
  const [slotSize, setSlotSize] = useState<SlotSize>('MEDIUM')
  const [reservedForId, setReservedForId] = useState('')
  const [reservationMode, setReservationMode] = useState<ReservationMode>('KEEP')

  const load = useCallback(() => {
    setError(null)
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

  useEffect(load, [load])

  const free = slots.filter((slot) => !slot.occupied).length
  const occupied = slots.length - free

  function resetSlotForm() {
    setEditingSlot(null)
    setSlotCode('')
    setSlotType('STANDARD')
    setSlotSize('MEDIUM')
    setReservedForId('')
    setReservationMode('KEEP')
  }

  function startSlotEdit(slot: SlotResponse) {
    setEditingSlot(slot)
    setSlotCode(slot.code)
    setSlotType(slot.type)
    setSlotSize(slot.size)
    setReservedForId('')
    setReservationMode('KEEP')
    setSlotError(null)
    setSlotMessage(null)
  }

  async function handleSlotSubmit(event: FormEvent) {
    event.preventDefault()
    setSlotError(null)
    setSlotMessage(null)
    setSlotBusy(true)
    try {
      if (editingSlot) {
        const reservedId =
          slotType === 'VIP' && reservationMode === 'SET' && reservedForId.trim() !== ''
            ? Number(reservedForId)
            : null
        const saved = await api.patch<SlotResponse>(`/api/slots/${encodeURIComponent(editingSlot.code)}`, {
          size: slotSize,
          reservedForId: reservedId,
          clearReservation: slotType === 'VIP' && reservationMode === 'CLEAR',
        })
        setSlotMessage(`Updated slot ${saved.code}.`)
      } else {
        const saved = await api.post<SlotResponse>('/api/slots', {
          code: slotCode,
          type: slotType,
          size: slotSize,
          reservedForId: slotType === 'VIP' && reservedForId.trim() !== '' ? Number(reservedForId) : null,
        })
        setSlotMessage(`Created slot ${saved.code}.`)
      }
      resetSlotForm()
      load()
    } catch (caught: unknown) {
      setSlotError(caught instanceof ApiError ? caught.message : 'Slot save failed.')
    } finally {
      setSlotBusy(false)
    }
  }

  async function handleSlotDelete(slot: SlotResponse) {
    if (!window.confirm(`Delete slot ${slot.code}?`)) {
      return
    }
    setSlotError(null)
    setSlotMessage(null)
    try {
      await api.delete<void>(`/api/slots/${encodeURIComponent(slot.code)}`)
      setSlotMessage(`Deleted slot ${slot.code}.`)
      if (editingSlot?.id === slot.id) {
        resetSlotForm()
      }
      load()
    } catch (caught: unknown) {
      setSlotError(caught instanceof ApiError ? caught.message : 'Slot delete failed.')
    }
  }

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
          value={occupancy ? `${occupancy.occupancyPercent}%` : '--'}
        />
      </div>

      {error && (
        <Alert variant="destructive" className="mb-5">
          <AlertCircle />
          <AlertTitle>Load failed</AlertTitle>
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      {user?.role === 'ADMIN' && (
        <Card className="mb-5">
          <CardHeader>
            <CardTitle>{editingSlot ? 'Edit slot' : 'Create slot'}</CardTitle>
            <CardDescription>{editingSlot ? editingSlot.code : 'Admin slot record'}</CardDescription>
          </CardHeader>
          <CardContent>
            <form className="grid gap-4 lg:grid-cols-[8rem_10rem_10rem_1fr_auto]" onSubmit={handleSlotSubmit}>
              <div className="space-y-2">
                <Label htmlFor="slot-code">Code</Label>
                <Input
                  id="slot-code"
                  value={slotCode}
                  onChange={(e) => setSlotCode(e.target.value.toUpperCase())}
                  className="font-mono uppercase"
                  disabled={editingSlot !== null}
                  required
                />
              </div>

              <div className="space-y-2">
                <Label>Type</Label>
                <Select
                  value={slotType}
                  onValueChange={(value) => setSlotType(value as SlotType)}
                  disabled={editingSlot !== null}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="STANDARD">Standard</SelectItem>
                    <SelectItem value="VIP">VIP</SelectItem>
                    <SelectItem value="DISABLED">Disabled</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label>Size</Label>
                <Select value={slotSize} onValueChange={(value) => setSlotSize(value as SlotSize)}>
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="SMALL">Small</SelectItem>
                    <SelectItem value="MEDIUM">Medium</SelectItem>
                    <SelectItem value="LARGE">Large</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label>{editingSlot && slotType === 'VIP' ? 'Reservation' : 'Reserved customer id'}</Label>
                {editingSlot && slotType === 'VIP' ? (
                  <div className="grid gap-2 sm:grid-cols-[10rem_1fr]">
                    <Select value={reservationMode} onValueChange={(value) => setReservationMode(value as ReservationMode)}>
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="KEEP">Keep</SelectItem>
                        <SelectItem value="SET">Set</SelectItem>
                        <SelectItem value="CLEAR">Clear</SelectItem>
                      </SelectContent>
                    </Select>
                    <Input
                      value={reservedForId}
                      onChange={(e) => setReservedForId(e.target.value)}
                      disabled={reservationMode !== 'SET'}
                      placeholder="Customer id"
                    />
                  </div>
                ) : (
                  <Input
                    value={reservedForId}
                    onChange={(e) => setReservedForId(e.target.value)}
                    disabled={slotType !== 'VIP'}
                    placeholder="Optional"
                  />
                )}
              </div>

              <div className="flex items-end gap-2">
                <Button type="submit" disabled={slotBusy}>
                  {slotBusy ? 'Saving...' : editingSlot ? 'Update' : 'Create'}
                </Button>
                {editingSlot && (
                  <Button type="button" variant="outline" onClick={resetSlotForm} title="Cancel edit">
                    <X className="size-4" />
                  </Button>
                )}
              </div>
            </form>

            {slotError && (
              <Alert variant="destructive" className="mt-4">
                <AlertCircle />
                <AlertTitle>Slot save failed</AlertTitle>
                <AlertDescription>{slotError}</AlertDescription>
              </Alert>
            )}
            {slotMessage && (
              <Alert variant="success" className="mt-4">
                <CheckCircle2 />
                <AlertTitle>Saved</AlertTitle>
                <AlertDescription>{slotMessage}</AlertDescription>
              </Alert>
            )}
          </CardContent>
        </Card>
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
                  <p className="mt-1 text-xs text-muted-foreground">Reserved: {slot.reservedFor}</p>
                )}
                {user?.role === 'ADMIN' && (
                  <div className="mt-3 flex gap-2">
                    <Button
                      type="button"
                      variant="outline"
                      size="sm"
                      onClick={() => startSlotEdit(slot)}
                      title="Edit slot"
                    >
                      <Edit3 className="size-4" />
                    </Button>
                    <Button
                      type="button"
                      variant="destructive"
                      size="sm"
                      onClick={() => handleSlotDelete(slot)}
                      title="Delete slot"
                    >
                      <Trash2 className="size-4" />
                    </Button>
                  </div>
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
