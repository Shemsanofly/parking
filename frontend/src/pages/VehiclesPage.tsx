import { useCallback, useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { AlertCircle, Car, CheckCircle2, Edit3, Trash2, X } from 'lucide-react'
import { api, ApiError } from '@/api/client'
import { useAuth } from '@/auth/useAuth'
import type { VehicleResponse, VehicleType } from '@/api/types'
import { EmptyState } from '@/components/empty-state'
import { PageHeader } from '@/components/page-header'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { formatTsh } from '@/lib/utils'

export function VehiclesPage() {
  const { user } = useAuth()
  const [vehicles, setVehicles] = useState<VehicleResponse[]>([])
  const [type, setType] = useState<VehicleType>('CAR')
  const [plateNumber, setPlateNumber] = useState('')
  const [doors, setDoors] = useState('4')
  const [axleCount, setAxleCount] = useState('2')
  const [sidecar, setSidecar] = useState('false')
  const [ownerId, setOwnerId] = useState('')
  const [editing, setEditing] = useState<VehicleResponse | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [message, setMessage] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  const load = useCallback(() => {
    api
      .get<VehicleResponse[]>('/api/vehicles')
      .then(setVehicles)
      .catch((caught: unknown) =>
        setError(caught instanceof ApiError ? caught.message : 'Could not load vehicles.'),
      )
  }, [])

  useEffect(load, [load])

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    setMessage(null)
    setBusy(true)
    try {
      const payload = {
        type,
        plateNumber,
        ownerId: ownerId === '' ? null : Number(ownerId),
        doors: type === 'CAR' ? Number(doors) : null,
        axleCount: type === 'TRUCK' ? Number(axleCount) : null,
        sidecar: type === 'MOTORCYCLE' ? sidecar === 'true' : null,
      }
      const saved = editing
        ? await api.put<VehicleResponse>(`/api/vehicles/${editing.id}`, payload)
        : await api.post<VehicleResponse>('/api/vehicles', payload)
      setMessage(editing ? `Updated ${saved.plateNumber}.` : `Registered ${saved.plateNumber}.`)
      resetForm()
      load()
    } catch (caught: unknown) {
      setError(caught instanceof ApiError ? caught.message : 'Save failed.')
    } finally {
      setBusy(false)
    }
  }

  function startEdit(vehicle: VehicleResponse) {
    const detailNumber = vehicle.detail.match(/\d+/)?.[0] ?? ''
    setEditing(vehicle)
    setType(vehicle.type)
    setPlateNumber(vehicle.plateNumber)
    setOwnerId(user?.role === 'ADMIN' ? String(vehicle.ownerId) : '')
    setDoors(vehicle.type === 'CAR' ? detailNumber || '4' : '4')
    setAxleCount(vehicle.type === 'TRUCK' ? detailNumber || '2' : '2')
    setSidecar(vehicle.type === 'MOTORCYCLE' && vehicle.detail.includes('with') ? 'true' : 'false')
    setError(null)
    setMessage(null)
  }

  function resetForm() {
    setEditing(null)
    setType('CAR')
    setPlateNumber('')
    setDoors('4')
    setAxleCount('2')
    setSidecar('false')
    setOwnerId('')
  }

  async function handleDelete(vehicle: VehicleResponse) {
    if (!window.confirm(`Delete vehicle ${vehicle.plateNumber}?`)) {
      return
    }
    setError(null)
    setMessage(null)
    try {
      await api.delete<void>(`/api/vehicles/${vehicle.id}`)
      setMessage(`Deleted ${vehicle.plateNumber}.`)
      if (editing?.id === vehicle.id) {
        resetForm()
      }
      load()
    } catch (caught: unknown) {
      setError(caught instanceof ApiError ? caught.message : 'Delete failed.')
    }
  }

  return (
    <>
      <PageHeader
        title="Vehicles"
        description="Plate format: T123ABC. Register before check-in."
      />

      <div className="grid gap-6 lg:grid-cols-[20rem_1fr]">
        <Card>
          <CardHeader>
            <CardTitle>{editing ? 'Edit vehicle' : 'Register'}</CardTitle>
            <CardDescription>{editing ? editing.plateNumber : 'Type + plate.'}</CardDescription>
          </CardHeader>
          <CardContent>
            <form className="space-y-4" onSubmit={handleSubmit}>
              <div className="space-y-2">
                <Label>Type</Label>
                <Select
                  value={type}
                  onValueChange={(value) => setType(value as VehicleType)}
                  disabled={editing !== null}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="CAR">Car · TSh 2,000/hr</SelectItem>
                    <SelectItem value="TRUCK">Truck · TSh 5,000/hr</SelectItem>
                    <SelectItem value="MOTORCYCLE">Motorcycle · TSh 1,000/hr</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label htmlFor="plate">Plate number</Label>
                <Input
                  id="plate"
                  value={plateNumber}
                  onChange={(e) => setPlateNumber(e.target.value.toUpperCase())}
                  placeholder="T123ABC"
                  className="font-mono uppercase"
                  required
                />
              </div>

              {type === 'CAR' && (
                <div className="space-y-2">
                  <Label htmlFor="doors">Doors</Label>
                  <Input id="doors" type="number" min={2} max={6} value={doors}
                         onChange={(e) => setDoors(e.target.value)} />
                </div>
              )}

              {type === 'TRUCK' && (
                <div className="space-y-2">
                  <Label htmlFor="axles">Axles</Label>
                  <Input id="axles" type="number" min={2} max={8} value={axleCount}
                         onChange={(e) => setAxleCount(e.target.value)} />
                </div>
              )}

              {type === 'MOTORCYCLE' && (
                <div className="space-y-2">
                  <Label>Sidecar</Label>
                  <Select value={sidecar} onValueChange={setSidecar}>
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="false">No</SelectItem>
                      <SelectItem value="true">Yes</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              )}

              {user?.role === 'ADMIN' && (
                <div className="space-y-2">
                  <Label htmlFor="owner">Owner customer id</Label>
                  <Input
                    id="owner"
                    value={ownerId}
                    onChange={(e) => setOwnerId(e.target.value)}
                    placeholder="2 for juma, 3 for neema"
                    required
                  />
                </div>
              )}

              {error && (
                <Alert variant="destructive">
                  <AlertCircle />
                  <AlertTitle>Save failed</AlertTitle>
                  <AlertDescription>{error}</AlertDescription>
                </Alert>
              )}
              {message && (
                <Alert variant="success">
                  <CheckCircle2 />
                  <AlertTitle>Saved</AlertTitle>
                  <AlertDescription>{message}</AlertDescription>
                </Alert>
              )}

              <Button type="submit" className="w-full" disabled={busy}>
                {busy ? 'Saving...' : editing ? 'Update vehicle' : 'Register vehicle'}
              </Button>
              {editing && (
                <Button type="button" variant="outline" className="w-full" onClick={resetForm}>
                  <X className="size-4" />
                  Cancel edit
                </Button>
              )}
            </form>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-base">
              {user?.role === 'ADMIN' ? 'All vehicles' : 'My vehicles'}
            </CardTitle>
            <CardDescription>
              {vehicles.length === 0
                ? 'Nothing registered yet.'
                : `${vehicles.length} registered`}
            </CardDescription>
          </CardHeader>
          <CardContent>
            {vehicles.length === 0 ? (
              <EmptyState
                icon={Car}
                title="No vehicles yet"
                description="Register a plate on the left, then use Check in to park it."
              />
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="text-muted-foreground border-b text-left text-xs uppercase">
                      <th className="pb-2 pr-3 font-medium">Plate</th>
                      <th className="pb-2 pr-3 font-medium">Type</th>
                      <th className="pb-2 pr-3 font-medium">Detail</th>
                      <th className="pb-2 pr-3 font-medium">Owner</th>
                      <th className="pb-2 pr-3 font-medium">Rate/hr</th>
                      <th className="pb-2 font-medium">Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {vehicles.map((vehicle) => (
                      <tr key={vehicle.id} className="border-b last:border-0">
                        <td className="py-3 pr-3 font-mono font-medium">{vehicle.plateNumber}</td>
                        <td className="py-3 pr-3">{vehicle.type}</td>
                        <td className="text-muted-foreground py-3 pr-3">{vehicle.detail}</td>
                        <td className="py-3 pr-3">{vehicle.ownerName}</td>
                        <td className="py-3 pr-3 font-mono tabular-nums">{formatTsh(vehicle.hourlyRate)}</td>
                        <td className="py-3">
                          <div className="flex gap-2">
                            <Button
                              type="button"
                              variant="outline"
                              size="sm"
                              onClick={() => startEdit(vehicle)}
                              title="Edit vehicle"
                            >
                              <Edit3 className="size-4" />
                            </Button>
                            <Button
                              type="button"
                              variant="destructive"
                              size="sm"
                              onClick={() => handleDelete(vehicle)}
                              title="Delete vehicle"
                            >
                              <Trash2 className="size-4" />
                            </Button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </>
  )
}
