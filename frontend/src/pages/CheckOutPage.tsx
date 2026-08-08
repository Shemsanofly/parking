import { useCallback, useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { Link } from '@/lib/router'
import { AlertCircle, CheckCircle2, Wallet } from 'lucide-react'
import { api, ApiError } from '@/api/client'
import type {
  MobileMoneyProvider,
  PaymentMethod,
  PaymentResponse,
  SessionResponse,
} from '@/api/types'
import { EmptyState } from '@/components/empty-state'
import { PageHeader } from '@/components/page-header'
import { SessionStatusBadge } from '@/components/session-status-badge'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { formatTsh } from '@/lib/utils'

export function CheckOutPage() {
  const [sessions, setSessions] = useState<SessionResponse[]>([])
  const [paying, setPaying] = useState<SessionResponse | null>(null)
  const [method, setMethod] = useState<PaymentMethod>('CASH')
  const [amountTendered, setAmountTendered] = useState('')
  const [cardLast4, setCardLast4] = useState('')
  const [provider, setProvider] = useState<MobileMoneyProvider>('MPESA')
  const [msisdn, setMsisdn] = useState('')
  const [receipt, setReceipt] = useState<PaymentResponse | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  const load = useCallback(() => {
    api
      .get<SessionResponse[]>('/api/sessions')
      .then((all) => setSessions(all.filter((session) => session.status !== 'CLOSED')))
      .catch((caught: unknown) =>
        setError(caught instanceof ApiError ? caught.message : 'Could not load sessions.'),
      )
  }, [])

  useEffect(load, [load])

  async function handleCheckOut(session: SessionResponse) {
    setError(null)
    setReceipt(null)
    setBusy(true)
    try {
      const closed = await api.post<SessionResponse>(`/api/sessions/${session.id}/check-out`)
      setPaying(closed)
      setAmountTendered(closed.fee === null ? '' : String(closed.fee))
      load()
    } catch (caught: unknown) {
      setError(caught instanceof ApiError ? caught.message : 'Check-out failed.')
    } finally {
      setBusy(false)
    }
  }

  function startPay(session: SessionResponse) {
    setError(null)
    setReceipt(null)
    setPaying(session)
    setAmountTendered(session.fee === null ? '' : String(session.fee))
  }

  async function handlePay(event: FormEvent) {
    event.preventDefault()
    if (!paying) return
    setError(null)
    setBusy(true)
    try {
      const response = await api.post<PaymentResponse>('/api/payments', {
        sessionId: paying.id,
        method,
        amountTendered: method === 'CASH' ? Number(amountTendered) : null,
        cardLast4: method === 'CARD' ? cardLast4 : null,
        provider: method === 'MOBILE_MONEY' ? provider : null,
        msisdn: method === 'MOBILE_MONEY' ? msisdn : null,
      })
      setReceipt(response)
      setPaying(null)
      load()
    } catch (caught: unknown) {
      setError(caught instanceof ApiError ? caught.message : 'Payment failed.')
    } finally {
      setBusy(false)
    }
  }

  return (
    <>
      <PageHeader
        title="Check out"
        description="Price the stay, then collect payment. Bay stays busy until paid."
      />

      {error && (
        <Alert variant="destructive" className="mb-6">
          <AlertCircle />
          <AlertTitle>Something went wrong</AlertTitle>
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      <div className="grid gap-6 lg:grid-cols-[1.2fr_0.8fr]">
        <Card>
          <CardHeader>
            <CardTitle>On site</CardTitle>
            <CardDescription>ACTIVE → price · UNPAID → collect.</CardDescription>
          </CardHeader>
          <CardContent>
            {sessions.length === 0 ? (
              <EmptyState
                icon={Wallet}
                title="Nothing parked"
                description="When a vehicle checks in, it will appear here for exit and payment."
                action={
                  <Button asChild variant="outline">
                    <Link to="/check-in">Go to check in</Link>
                  </Button>
                }
              />
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="text-muted-foreground border-b text-left text-xs uppercase">
                      <th className="pb-2 pr-3 font-medium">Plate</th>
                      <th className="pb-2 pr-3 font-medium">Slot</th>
                      <th className="pb-2 pr-3 font-medium">Status</th>
                      <th className="pb-2 pr-3 font-medium">Fee</th>
                      <th className="pb-2 font-medium" />
                    </tr>
                  </thead>
                  <tbody>
                    {sessions.map((session) => (
                      <tr key={session.id} className="border-b last:border-0">
                        <td className="py-3 pr-3 font-mono font-medium">{session.plateNumber}</td>
                        <td className="py-3 pr-3 font-mono">{session.slotCode}</td>
                        <td className="py-3 pr-3">
                          <SessionStatusBadge status={session.status} />
                        </td>
                        <td className="py-3 pr-3 font-mono tabular-nums">{formatTsh(session.fee)}</td>
                        <td className="py-3 text-right">
                          {session.status === 'ACTIVE' ? (
                            <Button
                              size="sm"
                              variant="outline"
                              disabled={busy}
                              onClick={() => void handleCheckOut(session)}
                            >
                              Price stay
                            </Button>
                          ) : (
                            <Button size="sm" onClick={() => startPay(session)}>
                              Take payment
                            </Button>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </CardContent>
        </Card>

        <div className="space-y-6">
          {paying ? (
            <Card>
              <CardHeader>
                <CardTitle className="text-base">
                  Pay {formatTsh(paying.fee)} for {paying.plateNumber}
                </CardTitle>
                <CardDescription>Slot {paying.slotCode} stays reserved until this succeeds.</CardDescription>
              </CardHeader>
              <CardContent>
                <form className="space-y-4" onSubmit={handlePay}>
                  <div className="space-y-2">
                    <Label>Payment method</Label>
                    <Select value={method} onValueChange={(value) => setMethod(value as PaymentMethod)}>
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="CASH">Cash</SelectItem>
                        <SelectItem value="CARD">Card</SelectItem>
                        <SelectItem value="MOBILE_MONEY">Mobile money</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>

                  {method === 'CASH' && (
                    <div className="space-y-2">
                      <Label htmlFor="tendered">Amount tendered</Label>
                      <Input
                        id="tendered"
                        type="number"
                        value={amountTendered}
                        onChange={(e) => setAmountTendered(e.target.value)}
                        required
                      />
                    </div>
                  )}

                  {method === 'CARD' && (
                    <div className="space-y-2">
                      <Label htmlFor="card">Card last 4 digits</Label>
                      <Input
                        id="card"
                        value={cardLast4}
                        maxLength={4}
                        placeholder="4242"
                        className="font-mono"
                        onChange={(e) => setCardLast4(e.target.value)}
                        required
                      />
                      <p className="text-muted-foreground text-xs">
                        Demo rule: cards ending in 0 are declined.
                      </p>
                    </div>
                  )}

                  {method === 'MOBILE_MONEY' && (
                    <>
                      <div className="space-y-2">
                        <Label>Provider</Label>
                        <Select
                          value={provider}
                          onValueChange={(value) => setProvider(value as MobileMoneyProvider)}
                        >
                          <SelectTrigger>
                            <SelectValue />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem value="MPESA">M-Pesa</SelectItem>
                            <SelectItem value="TIGO_PESA">Tigo Pesa</SelectItem>
                            <SelectItem value="AIRTEL_MONEY">Airtel Money</SelectItem>
                          </SelectContent>
                        </Select>
                      </div>
                      <div className="space-y-2">
                        <Label htmlFor="msisdn">Phone number</Label>
                        <Input
                          id="msisdn"
                          value={msisdn}
                          placeholder="255754000002"
                          className="font-mono"
                          onChange={(e) => setMsisdn(e.target.value)}
                          required
                        />
                      </div>
                    </>
                  )}

                  <Button type="submit" className="w-full" disabled={busy}>
                    {busy ? 'Processing…' : 'Confirm payment'}
                  </Button>
                </form>
              </CardContent>
            </Card>
          ) : (
            <Card>
              <CardContent className="text-muted-foreground py-10 text-center text-sm">
                Select a session on the left to price it or collect payment.
              </CardContent>
            </Card>
          )}

          {receipt && (
            <Card>
              <CardHeader>
                <CardTitle className="text-base">Receipt</CardTitle>
              </CardHeader>
              <CardContent className="space-y-3">
                <Alert variant="success">
                  <CheckCircle2 />
                  <AlertTitle>Payment successful</AlertTitle>
                  <AlertDescription>The slot is free again.</AlertDescription>
                </Alert>
                <dl className="space-y-2 text-sm">
                  <Row label="Method" value={receipt.method} />
                  <Row label="Amount" value={formatTsh(receipt.amount)} />
                  {receipt.changeGiven != null && (
                    <Row label="Change" value={formatTsh(receipt.changeGiven)} />
                  )}
                  <Row label="Reference" value={receipt.reference ?? '—'} mono />
                </dl>
              </CardContent>
            </Card>
          )}
        </div>
      </div>
    </>
  )
}

function Row({ label, value, mono }: { label: string; value: string; mono?: boolean }) {
  return (
    <div className="flex items-center justify-between gap-3 border-b py-2 last:border-0">
      <dt className="text-muted-foreground">{label}</dt>
      <dd className={mono ? 'font-mono font-medium' : 'font-medium'}>{value}</dd>
    </div>
  )
}
