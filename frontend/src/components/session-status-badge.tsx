import { Badge } from '@/components/ui/badge'
import type { SessionStatus } from '@/api/types'

export function SessionStatusBadge({ status }: { status: SessionStatus }) {
  if (status === 'ACTIVE') {
    return <Badge variant="outline">ACTIVE</Badge>
  }
  if (status === 'AWAITING_PAYMENT') {
    return <Badge variant="danger">UNPAID</Badge>
  }
  return <Badge variant="secondary">CLOSED</Badge>
}
