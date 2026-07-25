export type Role = 'ADMIN' | 'CUSTOMER'
export type VehicleType = 'CAR' | 'TRUCK' | 'MOTORCYCLE'
export type SlotType = 'STANDARD' | 'VIP' | 'DISABLED'
export type SlotSize = 'SMALL' | 'MEDIUM' | 'LARGE'
export type SessionStatus = 'ACTIVE' | 'AWAITING_PAYMENT' | 'CLOSED'
export type PaymentMethod = 'CASH' | 'CARD' | 'MOBILE_MONEY'
export type PaymentStatus = 'PENDING' | 'SUCCESSFUL' | 'FAILED'
export type MobileMoneyProvider = 'MPESA' | 'TIGO_PESA' | 'AIRTEL_MONEY'

export interface UserResponse {
  id: number
  username: string
  fullName: string
  role: Role
  disabilityPermit: boolean
}

export interface VehicleResponse {
  id: number
  plateNumber: string
  type: VehicleType
  ownerName: string
  ownerId: number
  hourlyRate: number
  detail: string
}

export interface SlotResponse {
  id: number
  code: string
  type: SlotType
  size: SlotSize
  occupied: boolean
  rateMultiplier: number
  reservedFor: string | null
}

export interface SessionResponse {
  id: number
  plateNumber: string
  slotCode: string
  operatorName: string
  entryTime: string
  exitTime: string | null
  fee: number | null
  status: SessionStatus
  ticketCode: string | null
}

export interface PaymentResponse {
  id: number
  sessionId: number
  method: PaymentMethod
  amount: number
  changeGiven: number | null
  reference: string | null
  status: PaymentStatus
  paidAt: string | null
}

export interface OccupancyReport {
  totalSlots: number
  occupiedSlots: number
  freeSlots: number
  occupancyPercent: number
  vehiclesOnSite: number
}

export interface RevenueReport {
  from: string
  to: string
  total: number
  closedSessions: number
}
